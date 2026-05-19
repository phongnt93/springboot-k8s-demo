@Library('jenkins-shared-library') _   // khai báo shared library

pipeline {
    agent any

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
                    echo "Building image: ${DOCKER_IMAGE_NAME}:${IMAGE_TAG}"
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                buildDockerImage(DOCKER_IMAGE_NAME, IMAGE_TAG)
            }
        }

        stage('Push to Docker Hub') {
            steps {
                pushToDockerHub(DOCKER_IMAGE_NAME, IMAGE_TAG, 'dockerhub-credentials')
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
            echo "CI/CD done: ${DOCKER_IMAGE_NAME}:${IMAGE_TAG} pushed and manifest updated"
        }
        failure {
            echo "Build failed"
        }
    }
}
