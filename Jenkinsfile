pipeline {
    agent any

    environment {
        DOCKER_IMAGE_NAME = 'nguyenphong8852/spring-boot-k8s-demo'
        IMAGE_TAG         = "${BUILD_NUMBER}"
        GIT_REPO          = 'https://github.com/phongnt93/springboot-k8s-demo.git'
        MANIFEST_FILE     = 'k8s-manifests/deployment.yaml'
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

        stage('Update K8s Manifest') {
            steps {
                withCredentials([usernamePassword(
                    credentialsId: 'github-credentials',
                    usernameVariable: 'GIT_USER',
                    passwordVariable: 'GIT_PASS'
                )]) {
                    sh '''
                      echo "=== Update image tag in deployment.yaml ==="

                      # Config git
                      git config user.email "jenkins@local"
                      git config user.name "Jenkins"

                      # Update image tag: thay :latest hoặc :<number> bằng tag mới
                      sed -i "s|image: ${DOCKER_IMAGE_NAME}:.*|image: ${DOCKER_IMAGE_NAME}:${IMAGE_TAG}|g" ${MANIFEST_FILE}

                      echo "=== Content after update ==="
                      grep "image:" ${MANIFEST_FILE}

                      # Commit và push
                      git add ${MANIFEST_FILE}
                      git commit -m "[Jenkins] Update image tag to ${IMAGE_TAG}"
                      git push https://${GIT_USER}:${GIT_PASS}@github.com/phongnt93/springboot-k8s-demo.git HEAD:main
                    '''
                }
            }
        }
    }

    post {
        success {
            echo "CI/CD done: ${DOCKER_IMAGE_NAME}:${IMAGE_TAG} pushed and manifest updated"
        }
        failure {
            echo "Build failed"
        }
    }
}
