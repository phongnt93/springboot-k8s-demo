@Library('jenkins-shared-library') _

pipeline {
    agent {
        kubernetes {
            label 'kaniko-agent'
            yaml """
apiVersion: v1
kind: Pod
metadata:
  labels:
    jenkins/label: kaniko-agent
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
    # mount secret Docker Hub vào /kaniko/.docker/config.json ở đây

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