pipeline {
    agent any

    environment {
        DOCKER_IMAGE_NAME = 'nguyenphong8852/spring-boot-k8s-demo'
        IMAGE_TAG         = "${BUILD_NUMBER}"
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
                script {
                    echo "Building image: ${DOCKER_IMAGE_NAME}:${IMAGE_TAG}"
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                sh '''
                  echo "=== Docker version ==="
                  docker version

                  echo "=== Build image ==="
                  docker build -t ${DOCKER_IMAGE_NAME}:${IMAGE_TAG} .
                  docker tag  ${DOCKER_IMAGE_NAME}:${IMAGE_TAG} ${DOCKER_IMAGE_NAME}:latest
                '''
            }
        }

        stage('Push to Docker Hub') {
            steps {
                withCredentials([usernamePassword(
                    credentialsId: 'dockerhub-credentials',
                    usernameVariable: 'DOCKER_USER',
                    passwordVariable: 'DOCKER_PASS'
                )]) {
                    sh '''
                      echo $DOCKER_PASS | docker login -u $DOCKER_USER --password-stdin
                      docker push ${DOCKER_IMAGE_NAME}:${IMAGE_TAG}
                      docker push ${DOCKER_IMAGE_NAME}:latest
                    '''
                }
            }
        }
    }

    post {
        success {
            echo "Pushed: ${DOCKER_IMAGE_NAME}:${IMAGE_TAG} and :latest"
        }
        failure {
            echo "Build failed"
        }
    }
}
