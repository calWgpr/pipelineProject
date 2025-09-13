pipeline {
  agent any

  tools { maven 'M3' }              // make sure a Maven installation named "M3" exists in Jenkins

  triggers {
    githubPush()
    pollSCM('H/2 * * * *')         // fallback poll every ~2 minutes
  }

  options {
    skipDefaultCheckout(true)
    timestamps()
  }

  environment {
    REPO_URL = 'https://github.com/calWgpr/pipelineProject.git'
    BRANCH = 'main'
    SONAR_SERVER = 'sonarqube'                // name configured in Jenkins -> Configure System -> SonarQube servers
    SONAR_PROJECT_KEY = 'pipelineProject'     // change if needed
    NEXUS_URL = 'http://localhost:8081/repository/maven-releases/'
  }

  stages {
    stage('Checkout') {
      steps {
        // robust checkout (clean, fetch everything)
        checkout([$class: 'GitSCM',
          branches: [[name: "*/${env.BRANCH}"]],
          userRemoteConfigs: [[url: "${env.REPO_URL}"]],
          extensions: [
            [$class: 'CleanBeforeCheckout'],
            [$class: 'PruneStaleBranch'],
            [$class: 'CloneOption', noTags: false, shallow: false, timeout: 20]
          ]
        ])

        script {
          echo "GIT_COMMIT (env): ${env.GIT_COMMIT}"
        }
      }
    }

    stage('SCM debug') {
      steps {
        script {
          if (isUnix()) {
            sh '''
              echo "=== remote -v ==="
              git remote -v
              echo "=== fetch ==="
              git fetch --all --tags
              echo "=== HEAD ==="
              git rev-parse HEAD || true
              echo "=== origin/${BRANCH} ==="
              git rev-parse origin/${BRANCH} || true
              echo "=== last 5 commits on origin/${BRANCH} ==="
              git --no-pager log -n5 origin/${BRANCH} --pretty=format:"%h %an %s"
            '''
          } else {
            bat """
              echo === remote -v ===
              git remote -v
              echo === fetch ===
              git fetch --all --tags
              echo === HEAD ===
              git rev-parse HEAD || echo .
              echo === origin/${BRANCH} ===
              git rev-parse origin/${BRANCH} || echo .
              echo === last 5 commits on origin/${BRANCH} ===
              git --no-pager log -n5 origin/${BRANCH} --pretty=format:"%h %an %s"
            """
          }
        }
      }
    }

    stage('Build') {
      steps {
        script {
          if (isUnix()) {
            sh 'mvn -v && mvn -B -U clean package'
          } else {
            bat 'mvn -v && mvn -B -U clean package'
          }
        }
      }
    }

    stage('Test') {
      steps {
        script {
          if (isUnix()) {
            sh 'mvn -B test'
          } else {
            bat 'mvn -B test'
          }
        }
      }
    }

    stage('SonarQube analysis') {
      steps {
        withSonarQubeEnv("${SONAR_SERVER}") {
          script {
            if (isUnix()) {
              sh "mvn -B sonar:sonar -Dsonar.projectKey=${SONAR_PROJECT_KEY} -Dsonar.host.url=http://localhost:9000"
            } else {
              bat "mvn -B sonar:sonar -Dsonar.projectKey=${SONAR_PROJECT_KEY} -Dsonar.host.url=http://localhost:9000"
            }
          }
        }
      }
    }

    stage('Quality Gate') {
      steps {
        timeout(time: 2, unit: 'MINUTES') {
          waitForQualityGate abortPipeline: true
        }
      }
    }

    stage('Publish to Nexus') {
      steps {
        withCredentials([usernamePassword(credentialsId: 'nexus-cred', usernameVariable: 'NEXUS_USER', passwordVariable: 'NEXUS_PASS')]) {
          script {
            if (isUnix()) {
              sh "mvn -B deploy -DaltDeploymentRepository=nexus::default::${NEXUS_URL} -Dnexus.user=$NEXUS_USER -Dnexus.pass=$NEXUS_PASS"
            } else {
              // Windows uses %VAR% expansion inside bat
              bat "mvn -B deploy -DaltDeploymentRepository=nexus::default::${NEXUS_URL} -Dnexus.user=%NEXUS_USER% -Dnexus.pass=%NEXUS_PASS%"
            }
          }
        }
      }
    }

    stage('Archive') {
      steps {
        archiveArtifacts artifacts: '**/target/*.jar', fingerprint: true
      }
    }
  }

  post {
    always {
      script {
        if (currentBuild.changeSets.size() == 0) {
          echo 'No changeSets detected (manual or scheduled run). Check the "SCM debug" output above for the actual commit that was fetched.'
        } else {
          for (cs in currentBuild.changeSets) {
            for (entry in cs.items) {
              echo "Commit: ${entry.commitId} by ${entry.author} — ${entry.msg}"
            }
          }
        }
      }
    }
  }
}
