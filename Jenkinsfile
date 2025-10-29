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
                    def version = sh(
                        script: 'mvn help:evaluate -Dexpression=project.version -q -DforceStdout',
                        returnStdout: true
                    ).trim()

                    withCredentials([string(credentialsId: 'dockerhub-pwd', variable: 'DOCKERHUB_PWD')]) {
                        sh """
                            docker build -t man17/country-service:${version} .
                            docker login -u man17 -p ${DOCKERHUB_PWD}
                            docker push man17/country-service:${version}
                        """
                    }
                }
            }
        }

        stage('Deploy micro-service (Docker)') {
            steps {
                sh '''
                    docker rm -f country-service || true
                    docker run -d --name country-service -p 8086:8086 man17/country-service:0.0.1-SNAPSHOT
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

        stage('Deploy WAR to Nexus') {
            steps {
                script {
                    def version = sh(
                        script: 'mvn help:evaluate -Dexpression=project.version -q -DforceStdout',
                        returnStdout: true
                    ).trim()

                    def isSnapshot = version.endsWith('-SNAPSHOT')
                    def groupId = "com.manar"
                    def artifactId = "country-service"

                    if (isSnapshot) {
                        echo "📦 Déploiement SNAPSHOT vers Nexus..."
                        sh """
                            mvn deploy:deploy-file \
                            -Dfile=target/country-service-${version}.war \
                            -DgroupId=${groupId} \
                            -DartifactId=${artifactId} \
                            -Dversion=${version} \
                            -Dpackaging=war \
                            -DrepositoryId=nexus-snapshots \
                            -Durl=http://localhost:8081/repository/maven-snapshots/
                        """
                    } else {
                        echo "🚀 Déploiement RELEASE vers Nexus..."
                        sh """
                            mvn deploy:deploy-file \
                            -Dfile=target/country-service-${version}.war \
                            -DgroupId=${groupId} \
                            -DartifactId=${artifactId} \
                            -Dversion=${version} \
                            -Dpackaging=war \
                            -DrepositoryId=nexus-releases \
                            -Durl=http://localhost:8081/repository/maven-releases/
                        """
                    }
                }
            }
        }

       
    }
}
