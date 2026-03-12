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
    image: gcr.io/kaniko-project/executor:debug
    command:
    - /busybox/cat
    tty: true
    volumeMounts:
    - name: docker-secret
      mountPath: /kaniko/.docker
  volumes:
  - name: docker-secret
    secret:
      secretName: dockerhub-secret
'''
            defaultContainer 'kaniko'
        }
    }

    environment {
        DOCKER_IMAGE_NAME = 'nguyenphong8852/spring-boot-k8s-demo'
        IMAGE_TAG         = "${BUILD_NUMBER}"
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
                script {
                    echo "Building image ${DOCKER_IMAGE_NAME}:${IMAGE_TAG}"
                }
            }
        }

        stage('Build & Push with Kaniko') {
            steps {
                sh '''
                  echo "=== Workspace ==="
                  pwd
                  ls -la

                  echo "=== Docker config ==="
                  ls -la /kaniko/.docker
                  head -c 120 /kaniko/.docker/config.json || true

                  echo "=== Build & Push ==="
                  /kaniko/executor \
                    --dockerfile=${PWD}/Dockerfile \
                    --context=dir://${PWD} \
                    --destination=${DOCKER_IMAGE_NAME}:${IMAGE_TAG} \
                    --destination=${DOCKER_IMAGE_NAME}:latest \
                    --verbosity=info
                '''
            }
        }
    }

    post {
        success {
            echo "✅ Pushed: ${DOCKER_IMAGE_NAME}:${IMAGE_TAG} and :latest"
        }
        failure {
            echo "❌ Build failed – xem log Kaniko ở trên"
        }
    }
}
