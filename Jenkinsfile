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
        DOCKER_IMAGE_NAME = 'nguyenphong8852/spring-boot-k8s-demo'
        DOCKER_TAG = "${BUILD_NUMBER}"
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
                script {
                    echo "Branch: ${env.BRANCH_NAME}"
                    echo "Commit: ${env.GIT_COMMIT}"
                    echo "Building image: ${DOCKER_IMAGE_NAME}:${DOCKER_TAG}"
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                container('docker') {
                    sh '''
                      echo "=== Docker Version ==="
                      docker version

                      echo "=== Building Image ==="
                      docker build -t ${DOCKER_IMAGE_NAME}:${DOCKER_TAG} .
                      docker tag ${DOCKER_IMAGE_NAME}:${DOCKER_TAG} ${DOCKER_IMAGE_NAME}:latest

                      echo "=== Image Built ==="
                      docker images | grep spring-boot-k8s-demo
                    '''
                }
            }
        }

        stage('Push to Docker Hub') {
            steps {
                container('docker') {
                    withCredentials([usernamePassword(
                        credentialsId: 'dockerhub-credentials',
                        usernameVariable: 'DOCKER_USER',
                        passwordVariable: 'DOCKER_PASS'
                    )]) {
                        sh '''
                          echo "=== Docker Login ==="
                          echo $DOCKER_PASS | docker login -u $DOCKER_USER --password-stdin

                          echo "=== Pushing Images ==="
                          docker push ${DOCKER_IMAGE_NAME}:${DOCKER_TAG}
                          docker push ${DOCKER_IMAGE_NAME}:latest

                          echo "=== Push Complete ==="
                          echo "Image available at:"
                          echo "  - ${DOCKER_IMAGE_NAME}:${DOCKER_TAG}"
                          echo "  - ${DOCKER_IMAGE_NAME}:latest"
                        '''
                    }
                }
            }
        }
    }

    post {
        success {
            echo """
            ╔════════════════════════════════════════════════════════════╗
            ║  ✅ BUILD SUCCESSFUL                                       ║
            ╠════════════════════════════════════════════════════════════╣
            ║  Image: ${DOCKER_IMAGE_NAME}:${DOCKER_TAG}                ║
            ║  Latest: ${DOCKER_IMAGE_NAME}:latest                      ║
            ║                                                            ║
            ║  Next Steps:                                               ║
            ║  1. ArgoCD will auto-sync (if configured)                 ║
            ║  2. Or manually: kubectl rollout restart -n springboot-demo║
            ║  3. Verify: kubectl get pods -n springboot-demo           ║
            ╚════════════════════════════════════════════════════════════╝
            """
        }
        failure {
            echo """
            ╔════════════════════════════════════════════════════════════╗
            ║  ❌ BUILD FAILED                                           ║
            ╠════════════════════════════════════════════════════════════╣
            ║  Check logs above for errors                              ║
            ║  Common issues:                                            ║
            ║  - Dockerfile syntax error                                ║
            ║  - Docker Hub credentials invalid                         ║
            ║  - Network connectivity                                   ║
            ╚════════════════════════════════════════════════════════════╝
            """
        }
        always {
            container('docker') {
                sh '''
                  echo "=== Cleanup Local Images ==="
                  docker rmi ${DOCKER_IMAGE_NAME}:${DOCKER_TAG} || true
                  docker rmi ${DOCKER_IMAGE_NAME}:latest || true
                  docker logout || true
                '''
            }
        }
    }
}
