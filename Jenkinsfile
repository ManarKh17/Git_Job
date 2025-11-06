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

        stage('Checkout Code') {
            steps {
                echo "📥 Clonage du projet depuis GitHub..."
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

        stage('Build & Push Docker Image') {
            steps {
                script {
                    def version = "${BUILD_NUMBER}"
                    def imageTag = "${DOCKER_USER}/${IMAGE_NAME}:${version}"

                    echo "🏗️ Construction et push de l'image Docker : ${imageTag}"

                    sh "docker build -t ${imageTag} ."

                    withCredentials([string(credentialsId: 'dockerhub-pwd', variable: 'DOCKER_PASS')]) {
                        sh '''
                            echo "$DOCKER_PASS" | docker login -u "man17" --password-stdin
                        '''
                    }

                    sh "docker push ${imageTag}"
                    sh "docker rmi ${imageTag} || true"
                }
            }
        }

        stage('Deploy using Ansible playbook') {
            steps {
                script {
                    echo "⚙️ Déploiement via Ansible..."
                    sh """
                        ansible-playbook playbookCICD.yml \
                        -e docker_registry_username=${DOCKER_USER} \
                        -e image_name=${IMAGE_NAME} \
                        -e image_tag=${BUILD_NUMBER}
                    """
                }
            }
        }

        stage('Verify Deployment') {
            steps {
                script {
                    echo "☸️ Vérification du déploiement Kubernetes..."
                    sh """
                        kubectl get pods -n ${NAMESPACE}
                        kubectl get svc -n ${NAMESPACE}
                    """
                }
            }
        }
    }

    post {
        always {
            echo "🧹 Nettoyage du workspace Jenkins..."
            cleanWs()
        }
        success {
            echo '✅ Ansible playbook executed successfully!'
        }
        failure {
            echo '❌ Ansible playbook execution failed!'
        }
    }
}
