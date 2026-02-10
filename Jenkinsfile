pipeline {
    agent any

    environment {
        // Docker Registry
        DOCKER_REGISTRY       = 'docker.io'
        DOCKER_IMAGE_NAME     = 'nguyenphong8852/springboot-k8s-demo'
        DOCKER_CREDENTIALS_ID = 'dockerhub-credentials'   // Credentials trong Jenkins

        // Thông tin build
        GIT_COMMIT_SHORT = ''
        IMAGE_TAG        = ''
        DEPLOY_ENV       = ''
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

                    // Tag: branch-commit-buildNumber (vd: main-a1b2c3-10)
                    IMAGE_TAG = "${env.BRANCH_NAME}-${GIT_COMMIT_SHORT}-${env.BUILD_NUMBER}"

                    // Map branch -> env (sẵn nếu sau này dùng staging/production)
                    DEPLOY_ENV = (env.BRANCH_NAME == 'main'    ? 'production' :
                                  env.BRANCH_NAME == 'staging' ? 'staging'   : 'dev')

                    echo "Commit: ${GIT_COMMIT_SHORT}"
                    echo "Image tag: ${IMAGE_TAG}"
                    echo "Deploy env (for ArgoCD): ${DEPLOY_ENV}"
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
                      docker tag ${DOCKER_IMAGE_NAME}:${IMAGE_TAG} \
                                 ${DOCKER_IMAGE_NAME}:${env.BRANCH_NAME}-latest
                    """
                }
            }
        }

        stage('Docker Push') {
            steps {
                script {
                    docker.withRegistry("https://${DOCKER_REGISTRY}", DOCKER_CREDENTIALS_ID) {
                        sh "docker push ${DOCKER_IMAGE_NAME}:${IMAGE_TAG}"
                        sh "docker push ${DOCKER_IMAGE_NAME}:${env.BRANCH_NAME}-latest"
                    }
                }
            }
        }

        stage('Update Manifests (GitOps)') {
            steps {
                script {
                    sh """
                      # Đang ở trong repo springboot-k8s-demo

                      git config user.name  "Jenkins CI"
                      git config user.email "jenkins@ci.local"

                      # Cập nhật image trong k8s-manifests/deployment.yaml
                      sed -i 's|image: .*|image: ${DOCKER_IMAGE_NAME}:${IMAGE_TAG}|g' k8s-manifests/deployment.yaml

                      git add k8s-manifests/deployment.yaml
                      git commit -m "Update image to ${IMAGE_TAG} (build ${env.BUILD_NUMBER})" || echo "No changes to commit"
                      git push origin ${env.BRANCH_NAME} || echo "Nothing to push"
                    """
                }
            }
        }

        stage('Notify ArgoCD') {
            steps {
                echo "ArgoCD Application (repo: springboot-k8s-demo, path: k8s-manifests, ns: springboot-demo)"
                echo "sẽ tự Sync và rollout deployment với image: ${DOCKER_IMAGE_NAME}:${IMAGE_TAG}."
            }
        }
    }

    post {
        success {
            echo "✅ CI OK: build + test + push image + update manifests. CD do ArgoCD thực hiện."
        }
        always {
            sh 'docker system prune -f || true'
            cleanWs()
        }
    }
}
