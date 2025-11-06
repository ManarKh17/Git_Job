pipeline {
    agent any

    tools {
        maven 'M2_Home'      // Maven configuré dans Jenkins
        jdk 'JDK17'          // JDK configuré dans Jenkins
    }

    environment {
        DOCKER_USER = 'man17'
        IMAGE_NAME = 'country-service'
        NAMESPACE = 'jenkins'
        ANSIBLE_PLAYBOOK = 'playbookCICD.yml'
    }

    stages {

        stage('Tool Install') {
            steps {
                echo "🔧 Vérification des outils..."
                sh '''
                    mvn -v
                    java -version
                    ansible --version
                    docker --version
                    kubectl version --client
                '''
            }
        }

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

                    env.IMAGE_TAG = version
                }
            }
        }

        stage('Deploy using Ansible') {
            steps {
                script {
                    echo "⚙️ Déploiement de l'application avec Ansible..."

                    // Exécution du playbook
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
