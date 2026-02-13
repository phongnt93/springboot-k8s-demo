pipeline {
    agent any

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
                    echo "Commit: ${GIT_COMMIT_SHORT}, Image tag: ${IMAGE_TAG}"
                }
            }
        }

        stage('Maven Build & Test') {
            steps {
                sh '''
                  mvn clean test
                  mvn package -DskipTests
                '''
            }
        }

        stage('Docker Build') {
            steps {
                script {
                    sh """
                      docker build -t ${DOCKER_IMAGE_NAME}:${IMAGE_TAG} .
                      docker tag ${DOCKER_IMAGE_NAME}:${IMAGE_TAG} ${DOCKER_IMAGE_NAME}:latest
                    """
                }
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

        stage('Update Manifests (GitOps)') {
            steps {
                script {
                    sh """
                      git config user.name  "Jenkins CI"
                      git config user.email "jenkins@ci.local"

                      # KHÔNG đổi tag nữa, giữ nguyên ':latest' trong k8s-manifests/deployment.yaml
                      git status
                    """
                }
            }
        }

        stage('Notify ArgoCD') {
            steps {
                echo "ArgoCD sẽ luôn pull image ${DOCKER_IMAGE_NAME}:latest (deployment.yaml)."
            }
        }
    }

    post {
        success {
            echo "CI OK: build & push image (tag build + latest). CD: ArgoCD dùng latest."
        }
        always {
            sh 'docker system prune -f || true'
            cleanWs()
        }
    }
}
