pipeline {
    agent {
        kubernetes {
            yaml '''
apiVersion: v1
kind: Pod
spec:
  serviceAccountName: jenkins
  containers:
  - name: jnlp
    image: jenkins/inbound-agent:latest
  - name: kaniko
    image: gcr.io/kaniko-project/executor:debug
    command:
    - /busybox/cat
    tty: true
    env:
    - name: DOCKER_CONFIG
      value: /kaniko/.docker/config.json
    volumeMounts:
    - name: docker-secret
      mountPath: /kaniko/.docker
  volumes:
  - name: docker-secret
    secret:
      secretName: dockerhub-secret
'''
            defaultContainer 'jnlp'
        }
    }

    environment {
        DOCKER_IMAGE_NAME = 'nguyenphong8852/spring-boot-k8s-demo'
        // Tag theo build number; đủ dùng cho ArgoCD rollback/debug
        IMAGE_TAG         = "${env.BUILD_NUMBER}"
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
                script {
                    echo "Branch: ${env.BRANCH_NAME}"
                    echo "Build image: ${DOCKER_IMAGE_NAME}:${IMAGE_TAG}"
                }
            }
        }

        stage('Verify Kaniko Env') {
            steps {
                container('kaniko') {
                    sh '''
                      echo "=== Docker config in /kaniko/.docker ==="
                      ls -la /kaniko/.docker || true
                      head -c 120 /kaniko/.docker/config.json || true

                      echo "=== Workspace ==="
                      pwd
                      ls -la

                      echo "=== Check Dockerfile ==="
                      if [ -f Dockerfile ]; then
                        echo "FOUND Dockerfile"
                      else
                        echo "MISSING Dockerfile" && exit 1
                      fi
                    '''
                }
            }
        }

        stage('Build & Push with Kaniko') {
            steps {
                container('kaniko') {
                    sh """
                      echo "=== Building & pushing image ==="
                      /kaniko/executor \\
                        --dockerfile=${PWD}/Dockerfile \\
                        --context=dir://${PWD} \\
                        --destination=${DOCKER_IMAGE_NAME}:${IMAGE_TAG} \\
                        --destination=${DOCKER_IMAGE_NAME}:latest \\
                        --verbosity=info
                    """
                }
            }
        }
    }

    post {
        success {
            echo "✅ Build OK: ${DOCKER_IMAGE_NAME}:${IMAGE_TAG} (and :latest)"
        }
        failure {
            echo "❌ Build FAILED – xem log stage Kaniko."
        }
    }
}
