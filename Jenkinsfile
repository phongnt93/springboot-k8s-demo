pipeline {
    agent {
        kubernetes {
            yaml '''
apiVersion: v1
kind: Pod
spec:
  serviceAccountName: jenkins
  containers:
  - name: docker
    image: docker:27.0.3-dind
    command: ["sleep"]
    args: ["9999999"]
    volumeMounts:
    - name: docker-sock
      mountPath: /var/run/docker.sock
  volumes:
  - name: docker-sock
    hostPath:
      path: /var/run/docker.sock
'''
            defaultContainer 'docker'
        }
    }

    stages {
        stage('Test Docker') {
            steps {
                container('docker') {
                    sh '''
                      echo "=== Step 1: Docker Version ==="
                      docker version

                      echo "=== Step 2: Check Dockerfile ==="
                      ls -la
                      cat Dockerfile | head -20

                      echo "=== Step 3: Test Build (dry-run) ==="
                      docker build --no-cache -t test-image:1 . || echo "Build failed!"

                      echo "=== Step 4: Test Login ==="
                      echo "Login test (without actual credentials)"
                    '''
                }
            }
        }
    }
}
