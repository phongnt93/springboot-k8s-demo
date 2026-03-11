pipeline {
    agent {
        kubernetes {
            yaml '''
apiVersion: v1
kind: Pod
spec:
  serviceAccountName: jenkins
  containers:
  - name: jnlp
    image: jenkins/inbound-agent:latest
  - name: kaniko
    image: gcr.io/kaniko-project/executor:debug
    command: ["/busybox/cat"]
    tty: true
    volumeMounts:
    - name: kaniko-secret
      mountPath: /kaniko/.docker
  volumes:
  - name: kaniko-secret
    secret:
      secretName: dockerhub-secret
      items:
      - key: .dockerconfigjson
        path: config.json
'''
            defaultContainer 'jnlp'
        }
    }

    // ... environment giữ nguyên

    stages {
        stage('Checkout') {
            steps {
                checkout scm
                script {
                    GIT_COMMIT_SHORT = sh(script: "git rev-parse --short HEAD", returnStdout: true).trim()
                    IMAGE_TAG = "${env.BRANCH_NAME}-${GIT_COMMIT_SHORT}-${env.BUILD_NUMBER}"
                }
            }
        }

        stage('Build & Push') {
            steps {
                container(name: 'kaniko', shell: '/busybox/sh') {
                    sh """
                      echo "Building ${DOCKER_IMAGE_NAME}:${IMAGE_TAG}"
                      /kaniko/executor \\
                        --dockerfile=Dockerfile \\
                        --context=dir://${PWD} \\
                        --destination=${DOCKER_IMAGE_NAME}:${IMAGE_TAG} \\
                        --destination=${DOCKER_IMAGE_NAME}:latest \\
                        --cache=true
                    """
                }
            }
        }
    }
}
