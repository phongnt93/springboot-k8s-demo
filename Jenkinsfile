@Library('jenkins-shared-library') _   // khai báo shared library

pipeline {
    // Dùng agent Kubernetes có container 'kaniko'
    agent {
        label 'jenkins-jenkins-agent'   // hoặc label mà pod template Kaniko của bạn đang dùng
    }

    environment {
        DOCKER_IMAGE_NAME = 'nguyenphong8852/spring-boot-k8s-demo'
        IMAGE_TAG         = "${BUILD_NUMBER}"
        MANIFEST_FILE     = 'k8s-manifests/deployment.yaml'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
                script {
                    echo "Building image (Kaniko): ${DOCKER_IMAGE_NAME}:${IMAGE_TAG}"
                }
            }
        }

        stage('Build & Push Image (Kaniko)') {
            steps {
                // Chạy lệnh Kaniko trong container 'kaniko'
                container('kaniko') {
                    sh """
                        /kaniko/executor \
                          --dockerfile=${WORKSPACE}/Dockerfile \
                          --context=${WORKSPACE} \
                          --destination=${DOCKER_IMAGE_NAME}:${IMAGE_TAG} \
                          --cleanup
                    """
                }
            }
        }

        // Stage này chỉ để log, vì Kaniko đã push luôn image ở stage trên
        stage('Push to Docker Hub') {
            steps {
                echo "Image đã được Kaniko build & push: ${DOCKER_IMAGE_NAME}:${IMAGE_TAG}"
            }
        }

        stage('Update K8s Manifest') {
            steps {
                updateK8sManifest(
                    DOCKER_IMAGE_NAME,
                    IMAGE_TAG,
                    MANIFEST_FILE,
                    'github-credentials',
                    'https://github.com/phongnt93/springboot-k8s-demo.git'
                )
            }
        }
    }

    post {
        success {
            echo "CI/CD done (Kaniko): ${DOCKER_IMAGE_NAME}:${IMAGE_TAG} pushed and manifest updated"
        }
        failure {
            echo "Build failed"
        }
    }
}