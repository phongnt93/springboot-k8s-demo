apiVersion: apps/v1
kind: Deployment
metadata:
  name: springboot-k8s-demo
  namespace: springboot-demo
spec:
  replicas: 1
  selector:
    matchLabels:
      app: springboot-k8s-demo
  template:
    metadata:
      labels:
        app: springboot-k8s-demo
    spec:
      imagePullSecrets:
        - name: dockerhub-secret
      containers:
        - name: springboot-k8s-demo
          image: nguyenphong8852/springboot-k8s-demo:latest
          ports:
            - containerPort: 8080
---
apiVersion: v1
kind: Service
metadata:
  name: springboot-k8s-demo
  namespace: springboot-demo
spec:
  type: ClusterIP
  selector:
    app: springboot-k8s-demo
  ports:
    - port: 80
      targetPort: 8080
---
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: springboot-k8s-demo
  namespace: springboot-demo
spec:
  ingressClassName: nginx
  tls:
    - secretName: argocd-tls
      hosts:
        - springboot-app.local
  rules:
    - host: springboot-app.local
      http:
        paths:
          - path: /
            pathType: Prefix
            backend:
              service:
                name: springboot-k8s-demo
                port:
                  number: 80
