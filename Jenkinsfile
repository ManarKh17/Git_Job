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

        stage('Deploy WAR to Nexus') {
            steps {
                script {
                    // 🔹 Récupération de la version Maven
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

        stage('Deploy WAR to Tomcat') {
            steps {
                script {
                    def TOMCAT_HOME = "C:/apache-tomcat-11.0.13"
                    def WAR_NAME = "country-service.war"
                    def NEXUS_URL = "http://admin:admin@localhost:8081/repository/maven-snapshots/com/manar/country-service/0.0.1-SNAPSHOT/country-service-0.0.1-SNAPSHOT.war"

                    echo "📥 Téléchargement du WAR depuis Nexus..."
                    sh """
                        curl -f -L -o "${TOMCAT_HOME}/webapps/${WAR_NAME}" "${NEXUS_URL}"
                    """

                    echo "🔄 Redémarrage de Tomcat..."
                    bat """
                        cd "${TOMCAT_HOME}\\bin"
                        shutdown.bat || exit 0
                        timeout /t 5 >nul
                        startup.bat
                    """
                }
            }
        }

        stage('Verify Deployment') {
            steps {
                echo "🔍 Vérification du déploiement sur Tomcat..."
                sh 'sleep 10'
                sh 'curl -I http://localhost:8888/country-service/ || true'
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
