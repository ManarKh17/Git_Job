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

        stage('Build & Test') {
            steps {
                sh 'mvn clean package -Dmaven.test.failure.ignore=true'
            }
            post {
                always {
                    junit allowEmptyResults: true, testResults: '**/target/surefire-reports/*.xml'
                }
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withCredentials([string(credentialsId: 'sonar-token', variable: 'SONAR_TOKEN')]) {
                    sh """
                        mvn sonar:sonar \
                        -Dsonar.projectKey=country-service \
                        -Dsonar.host.url=http://localhost:9000 \
                        -Dsonar.login=${SONAR_TOKEN}
                    """
                }
            }
        }

        stage('Build & Push Docker Image') {
            steps {
                script {
                    def version = "v3"
                    sh """
                        docker build -t man17/country-service:${version} .
                    """
                    withCredentials([usernamePassword(credentialsId: 'dockerhub-cred', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
                        sh """
                            echo "${DOCKER_PASS}" | docker login -u "${DOCKER_USER}" --password-stdin
                            docker push ${DOCKER_USER}/country-service:${version}
                        """
                    }
                }
            }
        }

        stage('Deploy micro-service (Docker)') {
            steps {
                sh '''
                    docker rm -f country-service || true
                    docker run -d --name country-service -p 8086:8086 man17/country-service:v3
                '''
            }
        }

        stage('Verify Deployment') {
            steps {
                echo "🔍 Vérification du déploiement sur Docker..."
                sh 'sleep 10'
                sh 'curl -I http://localhost:8086/ || true'
            }
        }
    }

    post {
        success {
            echo "✅ Pipeline exécuté avec succès !"
        }
        failure {
            echo "❌ Le pipeline a échoué. Vérifiez les logs Jenkins."
        }
    }
}
