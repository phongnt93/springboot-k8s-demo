@Library('jenkins-shared-library') _   // khai báo shared library

pipeline {
    agent {
        kubernetes {
            label 'kaniko-agent'
            yaml """
apiVersion: v1
kind: Pod
spec:
  containers:
  - name: jnlp
    image: jenkins/inbound-agent:3383.vc8881d4b_0e76-1
    env:
      - name: JENKINS_AGENT_WORKDIR
        value: /home/jenkins/agent
    volumeMounts:
      - name: workspace-volume
        mountPath: /home/jenkins/agent

  - name: kaniko
    image: gcr.io/kaniko-project/executor:debug
    command:
      - "/busybox/cat"
    tty: true
    volumeMounts:
      - name: workspace-volume
        mountPath: /home/jenkins/agent

  volumes:
  - name: workspace-volume
    emptyDir: {}
"""
        }
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
                // Lấy Docker Hub username/password từ Jenkins credentials
                withCredentials([
                    usernamePassword(
                        credentialsId: 'dockerhub-credentials',
                        usernameVariable: 'DOCKER_USERNAME',
                        passwordVariable: 'DOCKER_PASSWORD'
                    )
                ]) {
                    container('kaniko') {
                        // Tạo config.json cho Kaniko rồi build & push
                        sh '''
                            # Tạo thư mục config cho Kaniko
                            mkdir -p /kaniko/.docker

                            # Tạo file /kaniko/.docker/config.json với auth Docker Hub
                            cat > /kaniko/.docker/config.json <<EOF
{"auths":{"https://index.docker.io/v1/":{"auth":"$(printf "%s:%s" "$DOCKER_USERNAME" "$DOCKER_PASSWORD" | base64 | tr -d '\\n')"}}}
EOF

                            # Build & push image bằng Kaniko
                            /kaniko/executor \
                              --dockerfile=$WORKSPACE/Dockerfile \
                              --context=$WORKSPACE \
                              --destination=$DOCKER_IMAGE_NAME:$IMAGE_TAG \
                              --cleanup
                        '''
                    }
                }
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