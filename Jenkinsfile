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
            def isSnapshot = sh(
                script: 'mvn help:evaluate -Dexpression=project.version -q -DforceStdout',
                returnStdout: true
            ).trim().endsWith('-SNAPSHOT')

            def groupId = "com.manar"
            def artifactId = "country-service"

            if (isSnapshot) {
                sh """
                    mvn deploy:deploy-file \
                    -Dfile=target/country-service-0.0.1-SNAPSHOT.war \
                    -DgroupId=${groupId} \
                    -DartifactId=${artifactId} \
                    -Dversion=0.0.1-SNAPSHOT \
                    -Dpackaging=war \
                    -DrepositoryId=nexus-snapshots \
                    -Durl=http://localhost:8081/repository/maven-snapshots/
                """
            } else {
                sh """
                    mvn deploy:deploy-file \
                    -Dfile=target/country-service-0.0.1.war \
                    -DgroupId=${groupId} \
                    -DartifactId=${artifactId} \
                    -Dversion=0.0.1 \
                    -Dpackaging=war \
                    -DrepositoryId=nexus-releases \
                    -Durl=http://localhost:8081/repository/maven-releases/
                """
            }
        }
    }
}
