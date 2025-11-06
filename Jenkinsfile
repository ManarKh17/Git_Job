pipeline {
    agent any

    tools {
        maven 'M2_Home'   // Nom configuré dans Jenkins (Manage Jenkins → Global Tool Configuration)
        jdk 'JDK17'
    }

    environment {
        DOCKER_USER = 'man17'                    // ton username DockerHub
        IMAGE_NAME = 'country-service'           // nom de ton image Docker
        NAMESPACE = 'jenkins'                    // namespace Kubernetes
        ANSIBLE_PLAYBOOK = 'playbookCICD.yml'    // ton playbook Ansible
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

                    // Build de l'image
                    sh "docker build -t ${imageTag} ."

                    // Connexion et push vers DockerHub
                    withCredentials([string(credentialsId: 'dockerhub-pwd', variable: 'DOCKER_PASS')]) {
                        sh '''
                            echo "$DOCKER_PASS" | docker login -u "man17" --password-stdin
                        '''
                    }
                    sh "docker push ${imageTag}"

                    // Nettoyage local
                    sh "docker rmi ${imageTag} || true"

                    // Sauvegarde du tag pour Ansible
                    env.IMAGE_TAG = version
                }
            }
        }

        stage('Deploy using Ansible') {
            steps {
                script {
                    echo "⚙️ Exécution du playbook Ansible pour déployer sur Kubernetes..."

                    // Exécution du playbook Ansible
                    sh """
                        ansible-playbook ${ANSIBLE_PLAYBOOK} \
                        -e docker_registry_username=${DOCKER_USER} \
                        -e image_name=${IMAGE_NAME} \
                        -e image_tag=${IMAGE_TAG}
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
            echo "✅ Pipeline exécuté avec succès — Application déployée via Ansible et Kubernetes !"
        }
        failure {
            echo "❌ Le pipeline a échoué. Consulte les logs Jenkins pour les détails."
        }
    }
}
