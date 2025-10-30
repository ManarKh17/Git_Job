pipeline {
    agent any

    tools {
        maven 'M2_Home'
        jdk 'JDK17'
    }

    environment {
        DOCKER_USER = 'man17'
        IMAGE_NAME = 'country-service'
        NAMESPACE = 'jenkins'
    }

    stages {

        stage('Checkout') {
            steps {
                git(
                    url: 'https://github.com/ManarKh17/Git_Job.git',
                    branch: 'main',
                    credentialsId: 'github-token'
                )
            }
        }

        stage('Build Maven') {
            steps {
                echo "🚀 Compilation du projet avec Maven..."
                sh 'mvn clean package -DskipTests=true'
            }
            post {
                always {
                    junit allowEmptyResults: true, testResults: '**/target/surefire-reports/*.xml'
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                script {
                    def version = "${BUILD_NUMBER}"
                    def imageTag = "${DOCKER_USER}/${IMAGE_NAME}:${version}"

                    echo "🏗️ Construction de l'image Docker : ${imageTag}"

                    // Build de l'image
                    sh "docker build -t ${imageTag} ."

                    // Connexion à DockerHub
                    withCredentials([string(credentialsId: 'dockerhub-pwd', variable: 'DOCKER_PASS')]) {
                        sh '''
                            echo "$DOCKER_PASS" | docker login -u "man17" --password-stdin
                        '''
                    }

                    // Push sur DockerHub
                    sh "docker push ${imageTag}"

                    // Nettoyage local
                    sh "docker rmi ${imageTag} || true"
                }
            }
        }

        stage('Deploy to Kubernetes') {
            steps {
                script {
                    echo "☸️ Déploiement sur Kubernetes..."

                    kubeconfig(credentialsId: 'kubeconfig-jenkins', serverUrl: '') {
                        sh """
                            sed -i 's|man17/country-service:.*|man17/country-service:${BUILD_NUMBER}|' deployment.yaml
                            kubectl apply -f deployment.yaml -n ${NAMESPACE}
                            kubectl apply -f service.yaml -n ${NAMESPACE}
                            kubectl rollout status deployment/${IMAGE_NAME} -n ${NAMESPACE}
                        """
                    }
                }
            }
        }

        stage('Verify Deployment') {
            steps {
                script {
                    kubeconfig(credentialsId: 'kubeconfig-jenkins', serverUrl: '') {
                        sh """
                            echo '📦 Vérification du déploiement...'
                            kubectl get pods -n ${NAMESPACE}
                            kubectl get svc -n ${NAMESPACE}
                        """
                    }
                }
            }
        }
    }

    post {
        success {
            echo "✅ Pipeline exécuté avec succès — Application déployée sur Kubernetes !"
        }
        failure {
            echo "❌ Le pipeline a échoué. Consulte les logs Jenkins pour les détails."
        }
    }
}
