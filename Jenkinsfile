pipeline {
    agent {
        kubernetes {
            yaml '''
apiVersion: v1
kind: Pod
spec:
  serviceAccountName: jenkins
  containers:
  - name: docker
    image: docker:27.0.3-dind
    command:
    - sleep
    args:
    - "9999999"
    volumeMounts:
    - name: docker-sock
      mountPath: /var/run/docker.sock
  volumes:
  - name: docker-sock
    hostPath:
      path: /var/run/docker.sock
      type: Socket
'''
            defaultContainer 'docker'
        }
    }

    environment {
        DOCKER_REGISTRY       = 'docker.io'
        DOCKER_IMAGE_NAME     = 'nguyenphong8852/spring-boot-k8s-demo'
        DOCKER_CREDENTIALS_ID = 'dockerhub-credentials'

        GIT_COMMIT_SHORT = ''
        IMAGE_TAG        = ''
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
                script {
                    GIT_COMMIT_SHORT = sh(
                        script: "git rev-parse --short HEAD",
                        returnStdout: true
                    ).trim()
                    IMAGE_TAG = "${env.BRANCH_NAME}-${GIT_COMMIT_SHORT}-${env.BUILD_NUMBER}"
                    echo "Image version tag: ${IMAGE_TAG}"
                }
            }
        }

        stage('Docker Build') {
            steps {
                sh """
                  docker version
                  docker build -t ${DOCKER_IMAGE_NAME}:${IMAGE_TAG} .
                  docker tag ${DOCKER_IMAGE_NAME}:${IMAGE_TAG} ${DOCKER_IMAGE_NAME}:latest
                """
            }
        }

        stage('Docker Push') {
            steps {
                script {
                    docker.withRegistry("https://${DOCKER_REGISTRY}", DOCKER_CREDENTIALS_ID) {
                        sh "docker push ${DOCKER_IMAGE_NAME}:${IMAGE_TAG}"
                        sh "docker push ${DOCKER_IMAGE_NAME}:latest"
                    }
                }
            }
        }

        stage('Verify image') {
            steps {
                sh """
                  echo "Verify image just pushed:"
                  docker pull ${DOCKER_IMAGE_NAME}:latest
                  docker images | grep spring-boot-k8s-demo || true
                """
            }
        }
    }
}
