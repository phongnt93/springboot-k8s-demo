pipeline {
    agent any

    environment {
        // Docker Registry
        DOCKER_REGISTRY       = 'docker.io'
        DOCKER_IMAGE_NAME     = 'nguyenphong8852/springboot-k8s-demo'
        DOCKER_CREDENTIALS_ID = 'dockerhub-credentials'   // Credentials trong Jenkins

        // GitOps manifest repo (cho ArgoCD)
        MANIFEST_REPO_URL = 'https://github.com/phongnt93/k8s-manifests.git'
        GITHUB_TOKEN      = credentials('github-token')    // PAT có quyền push

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

                    // Tag dạng: branch-commit-buildNumber (vd: dev-a1b2c3-10)
                    IMAGE_TAG = "${env.BRANCH_NAME}-${GIT_COMMIT_SHORT}-${env.BUILD_NUMBER}"

                    // Map branch -> env
                    DEPLOY_ENV = (env.BRANCH_NAME == 'main'    ? 'production' :
                                  env.BRANCH_NAME == 'staging' ? 'staging'   : 'dev')

                    echo "Commit: ${GIT_COMMIT_SHORT}"
                    echo "Image tag: ${IMAGE_TAG}"
                    echo "Deploy env: ${DEPLOY_ENV}"
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
                      rm -rf k8s-manifests
                      git clone https://${GITHUB_TOKEN}@github.com/phongnt93/k8s-manifests.git
                      cd k8s-manifests

                      git config user.name  "Jenkins CI"
                      git config user.email "jenkins@ci.local"

                      # Cập nhật image trong deployment của môi trường tương ứng
                      # Ví dụ file: dev/deployment.yaml, staging/deployment.yaml, production/deployment.yaml
                      sed -i 's|image: .*|image: ${DOCKER_IMAGE_NAME}:${IMAGE_TAG}|g' ${DEPLOY_ENV}/deployment.yaml

                      git add ${DEPLOY_ENV}/deployment.yaml
                      git commit -m "Update ${DEPLOY_ENV} image to ${IMAGE_TAG} (build ${env.BUILD_NUMBER})" || echo "No changes to commit"
                      git push origin main || echo "Nothing to push"

                      cd ..
                      rm -rf k8s-manifests
                    """
                }
            }
        }

        stage('Notify ArgoCD') {
            when {
                anyOf {
                    branch 'main'
                    branch 'staging'
                    branch 'dev'
                }
            }
            steps {
                echo "ArgoCD sẽ tự động sync app trỏ tới path ${DEPLOY_ENV}/ trong repo phongnt93/k8s-manifests."
                echo "Nếu cần sync tay: argocd app sync springboot-${DEPLOY_ENV}"
            }
        }
    }

    post {
        success {
            echo "✅ CI hoàn tất: build + test + push image + update manifests. CD do ArgoCD thực hiện vào namespace springboot-demo."
        }
        always {
            sh 'docker system prune -f || true'
            cleanWs()
        }
    }
}

