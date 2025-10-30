pipeline {
    agent any

    tools {
        maven 'M2_Home'
        jdk 'JDK17'
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
                sh 'mvn clean package -Dmaven.test.failure.ignore=true'
            }
            post {
                always {
                    junit allowEmptyResults: true, testResults: '**/target/surefire-reports/*.xml'
                }
            }
        }

        stage('Build Dockerfile') {
            steps {
                script {
                    // 🔹 Version du build (numéro Jenkins)
                    def version = "${BUILD_NUMBER}"
                    def imageName = "man17/country-service:${version}"

                    // 🔹 Construction de l'image Docker
                    sh """
                        docker build -t ${imageName} .
                    """

                    // 🔹 Connexion à DockerHub
                    withCredentials([string(credentialsId: 'dockerhub-pwd', variable: 'DOCKER_PASS')]) {
                        sh """
                            echo "${DOCKER_PASS}" | docker login -u "man17" --password-stdin
                        """
                    }

                    // 🔹 Push sur DockerHub
                    sh """
                        docker push ${imageName}
                    """
                }
            }
        }

        stage('Deploy to Kubernetes') {
            steps {
                script {
                    // 🔹 Connexion Kubernetes via Credentials Jenkins
                    kubeconfig(credentialsId: 'kubeconfig-file', serverUrl: '') {
                        // Mise à jour des fichiers YAML avant déploiement
                        sh '''
                            sed -i "s|man17/country-service:.*|man17/country-service:${BUILD_NUMBER}|" deployment.yaml
                            kubectl apply -f deployment.yaml -n jenkins
                            kubectl apply -f service.yaml -n jenkins
                            kubectl rollout status deployment/country-service -n jenkins
                        '''
                    }
                }
            }
        }

        stage('Verify Deployment') {
            steps {
                script {
                    kubeconfig(credentialsId: 'kubeconfig-file', serverUrl: '') {
                        sh '''
                            kubectl get pods -n jenkins
                            kubectl get svc -n jenkins
                        '''
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
