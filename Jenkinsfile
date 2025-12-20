pipeline {
    agent any

    environment {
        // 서비스 이름 및 네임스페이스 정의
        APP_NAME        = "promotion-service"
        NAMESPACE       = "next-me"

        // GHCR 레지스트리 정보 (프로모션 서비스 전용 이미지 경로)
        REGISTRY        = "ghcr.io"
        GH_OWNER        = "sparta-next-me"
        IMAGE_REPO      = "promotion-service"
        FULL_IMAGE      = "${REGISTRY}/${GH_OWNER}/${IMAGE_REPO}:latest"

        // 시간대 설정
        TZ              = "Asia/Seoul"
    }

    stages {
        stage('Checkout') {
            steps {
                // GitHub로부터 소스 코드 최신본 가져오기
                checkout scm
            }
        }

        stage('Build & Test') {
            steps {
                // Jenkins에 등록된 promotion-service 전용 .env 파일을 사용하여 빌드
                withCredentials([
                    file(credentialsId: 'promotion-service-env-file', variable: 'ENV_FILE')
                ]) {
                    sh '''
                      # .env 파일을 환경변수로 로드하여 테스트 및 JAR 빌드 수행
                      set -a
                      . "$ENV_FILE"
                      set +a
                      ./gradlew clean bootJar --no-daemon
                    '''
                }
            }
        }

        stage('Docker Build & Push') {
            steps {
                // GHCR 로그인을 위한 공용 Credential 사용
                withCredentials([
                    usernamePassword(
                        credentialsId: 'ghcr-credential',
                        usernameVariable: 'USER',
                        passwordVariable: 'TOKEN'
                    )
                ]) {
                    sh """
                      # 도커 이미지 빌드
                      docker build -t ${FULL_IMAGE} .

                      # GHCR 로그인 후 이미지 푸시
                      echo "${TOKEN}" | docker login ${REGISTRY} -u "${USER}" --password-stdin
                      docker push ${FULL_IMAGE}
                    """
                }
            }
        }

        stage('Deploy to Kubernetes') {
            steps {
                // K3s 설정파일과 프로모션 전용 .env 파일을 사용하여 배포
                withCredentials([
                    file(credentialsId: 'k3s-kubeconfig', variable: 'KUBECONFIG_FILE'),
                    file(credentialsId: 'promotion-service-env-file', variable: 'ENV_FILE')
                ]) {
                    sh '''
                      export KUBECONFIG=${KUBECONFIG_FILE}

                      # 1. 기존 시크릿 삭제 후 .env 파일 기반으로 프로모션 전용 시크릿 새로 생성
                      echo "Updating K8s Secret: promotion-service-env..."
                      kubectl delete secret promotion-service-env -n ${NAMESPACE} --ignore-not-found
                      kubectl create secret generic promotion-service-env --from-env-file=${ENV_FILE} -n ${NAMESPACE}

                      # 2. 쿠버네티스 매니페스트 적용 (promotion-service.yaml)
                      echo "Applying manifests from promotion-service.yaml..."
                      kubectl apply -f promotion-service.yaml -n ${NAMESPACE}

                      # 3. 배포 모니터링: 신규 파드가 정상 가동(Ready)될 때까지 대기
                      # 이 단계에서 헬스체크 실패 시 Jenkins 빌드도 실패로 처리됨
                      echo "Monitoring rollout status..."
                      kubectl rollout status deployment/promotion-service -n ${NAMESPACE}

                      # 4. 배포 결과 최종 확인
                      kubectl get pods -n ${NAMESPACE} -l app=promotion-service
                    '''
                }
            }
        }
    }

    post {
        always {
            // 빌드 서버 용량 확보를 위해 빌드에 사용된 로컬 이미지는 즉시 삭제
            echo "Cleaning up local docker image..."
            sh "docker rmi ${FULL_IMAGE} || true"
        }
        success {
            echo "Successfully deployed ${APP_NAME} to Kubernetes Cluster!"
        }
        failure {
            echo "Deployment failed. Please check the Jenkins console logs and Pod health."
        }
    }
}