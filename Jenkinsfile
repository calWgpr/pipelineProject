pipeline {
  agent any

  // If you have a Maven tool named "M3" in Jenkins (Global Tool Config)
  tools { maven "M3" }

  // Build on every push via webhook (if configured) and also poll every 2 min as fallback
  triggers {
    githubPush()           // requires GitHub plugin + webhook
    pollSCM('H/2 * * * *') // fallback: polls every ~2 minutes
  }

  options {
    skipDefaultCheckout(true) // we'll do an explicit checkout with clean
    timestamps()
  }

  stages {
    stage('Checkout') {
      steps {
        // Replace URL and credentialsId if private
        checkout([$class: 'GitSCM',
          branches: [[name: '*/main']],
          userRemoteConfigs: [[
            url: 'https://github.com/calWgpr/pipelineProject.git',
            // credentialsId: 'github-cred-id' // <-- uncomment if private
          ]],
          extensions: [
            [$class: 'CleanBeforeCheckout'],
            [$class: 'PruneStaleBranch']
          ]
        ])

        script {
          // Show the exact commit pulled
          echo "GIT_COMMIT: ${env.GIT_COMMIT}"
        }
      }
    }

    stage('Build') {
      steps {
        // Windows agent:
        bat 'mvn -v && mvn -B -U clean package'
        // Linux agent (if you use Linux node instead):
        // sh 'mvn -v && mvn -B -U clean package'
      }
    }

    stage('Test') {
      steps {
        // Windows:
        bat 'mvn -B test'
        // Linux:
        // sh 'mvn -B test'
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
      // Print commits that triggered this build
      script {
        if (currentBuild.changeSets.size() == 0) {
          echo 'No changeSets detected (manual or scheduled run).'
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
