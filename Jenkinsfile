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

        // Docker build & push is handled inside the Ansible playbook (as per course template)

        stage('Deploy using Ansible') {
            steps {
                script {
                    echo "⚙️ Déploiement de l'application avec Ansible..."
                    withCredentials([string(credentialsId: 'dockerhub-pwd', variable: 'DOCKER_PASS')]) {
                        sh '''
                            ansible-playbook -i hosts ${ANSIBLE_PLAYBOOK} \
                            --skip-tags deps \
                            -e docker_registry_username="$DOCKER_USER" \
                            -e docker_registry_password="$DOCKER_PASS" \
                            -e image_name="$IMAGE_NAME" \
                            -e image_tag='v1'
                        '''
                    }
                }
            }
        }

        stage('Verify Deployment') {
            steps {
                script {
                    echo "☸️ Vérification du déploiement Kubernetes..."
                    sh """
                        export KUBECONFIG=./config
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
