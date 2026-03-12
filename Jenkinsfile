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
                  - 9999999
                  volumeMounts:
                  - name: docker-sock
                    mountPath: /var/run/docker.sock
                volumes:
                - name: docker-sock
                  hostPath:
                    path: /var/run/docker.sock
                    type: Socket
            '''
        }
    }

    environment {
        DOCKER_IMAGE_NAME = 'nguyenphong8852/spring-boot-k8s-demo'
        DOCKER_TAG = "${BUILD_NUMBER}"
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build Docker Image') {
            steps {
                container('docker') {
                    sh 'docker version'
                    sh 'docker build -t ${DOCKER_IMAGE_NAME}:${DOCKER_TAG} .'
                    sh 'docker tag ${DOCKER_IMAGE_NAME}:${DOCKER_TAG} ${DOCKER_IMAGE_NAME}:latest'
                }
            }
        }

        stage('Push to Docker Hub') {
            steps {
                container('docker') {
                    withCredentials([usernamePassword(credentialsId: 'dockerhub-credentials', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
                        sh '''
                          echo $DOCKER_PASS | docker login -u $DOCKER_USER --password-stdin
                          docker push ${DOCKER_IMAGE_NAME}:${DOCKER_TAG}
                          docker push ${DOCKER_IMAGE_NAME}:latest
                        '''
                    }
                }
            }
        }
    }

    post {
        success {
            echo "✅ Build succeeded! Image: ${DOCKER_IMAGE_NAME}:${DOCKER_TAG}"
        }
        failure {
            echo "❌ Build failed!"
        }
    }
}
