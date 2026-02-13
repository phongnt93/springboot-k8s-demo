pipeline {
    agent {
        kubernetes {
            yaml '''
apiVersion: v1
kind: Pod
spec:
  serviceAccountName: jenkins
  containers:
  - name: kaniko
    image: gcr.io/kaniko-project/executor:latest
    volumeMounts:
    - name: kaniko-secret
      mountPath: /kaniko/.docker
  volumes:
  - name: kaniko-secret
    secret:
      secretName: dockerhub-secret
      items:
      - key: .dockerconfigjson
        path: config.json
'''
            defaultContainer 'kaniko'
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

        stage('Build & Push with Kaniko') {
            steps {
                sh """
                  echo "Building and pushing image with Kaniko..."
                  /kaniko/executor \
                    --dockerfile=Dockerfile \
                    --context=${PWD} \
                    --destination=${DOCKER_IMAGE_NAME}:${IMAGE_TAG} \
                    --destination=${DOCKER_IMAGE_NAME}:latest
                """
            }
        }

        stage('Verify image') {
            steps {
                sh """
                  echo "Verify image just pushed:"
                  docker pull ${DOCKER_IMAGE_NAME}:latest || echo "docker CLI not available in kaniko pod"
                """
            }
        }
    }
}
