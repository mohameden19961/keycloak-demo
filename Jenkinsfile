pipeline {
    agent any

    environment {
        PROJECT_DIR = '/home/abdy/projects/keycloak-demo'
        // Load secrets from .env (not checked into git)
        SONAR_TOKEN = sh(script: "grep SONAR_TOKEN $PROJECT_DIR/.env | cut -d= -f2", returnStdout: true).trim()
        SONAR_HOST_URL = sh(script: "grep SONAR_HOST_URL $PROJECT_DIR/.env | cut -d= -f2", returnStdout: true).trim()
        NVD_API_KEY = sh(script: "grep NVD_API_KEY $PROJECT_DIR/.env | cut -d= -f2", returnStdout: true).trim()
    }

    stages {
        stage('Start Stack') {
            steps {
                sh 'cd $PROJECT_DIR && docker-compose up -d --build'
                sh '''
                    echo "Waiting for services..."
                    for i in $(seq 1 30); do
                        HEALTH=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:18090/api/public/health 2>/dev/null || echo "000")
                        KEYCLOAK=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:18082/realms/Taks/.well-known/openid-configuration 2>/dev/null || echo "000")
                        if [ "$HEALTH" = "200" ] && [ "$KEYCLOAK" = "200" ]; then
                            echo "Both services ready after ${i}s"
                            break
                        fi
                        echo "Waiting... app=$HEALTH keycloak=$KEYCLOAK"
                        sleep 3
                    done
                '''
            }
        }

        stage('Run Newman Tests') {
            steps {
                sh '''
                    cd $PROJECT_DIR && \
                    docker run --rm --network=host \
                      -v $PROJECT_DIR/tests:/etc/newman \
                      postman/newman:alpine \
                      run /etc/newman/task-api-collection.json \
                      --reporters cli,junit \
                      --reporter-junit-export /etc/newman/results.xml
                '''
            }
        }

        stage('SonarQube Analysis') {
            steps {
                sh '''
                    cd $PROJECT_DIR
                    ./mvnw compile sonar:sonar \
                      -Dsonar.projectKey=keycloak-demo \
                      -Dsonar.host.url=$SONAR_HOST_URL \
                      -Dsonar.login=$SONAR_TOKEN \
                      -DskipTests=true
                '''
            }
        }

        stage('OWASP Dependency Check') {
            steps {
                timeout(time: 6, unit: 'MINUTES') {
                    sh '''
                        cd $PROJECT_DIR
                        ./mvnw org.owasp:dependency-check-maven:check \
                          -DnvdApiKey=$NVD_API_KEY
                    '''
                }
            }
        }

        stage('Trivy Scan') {
            steps {
                sh '''
                    cd $PROJECT_DIR
                    trivy image --severity HIGH,CRITICAL --exit-code 0 keycloak-demo-app
                '''
            }
        }
    }

    post {
        always {
            sh 'cd $PROJECT_DIR && docker-compose down'
            sh 'cp $PROJECT_DIR/tests/results.xml $WORKSPACE/results.xml || true'
            sh 'cp $PROJECT_DIR/target/dependency-check-report.xml $WORKSPACE/ || true'
            junit allowEmptyResults: true, testResults: 'results.xml'
            dependencyCheckPublisher pattern: 'dependency-check-report.xml'
        }
    }
}
