# 🏗️ BookStore Architecture

## 📌 Tổng quan

BookStore là một ứng dụng **Spring Boot** quản lý bán sách trực tuyến, được thiết kế để phục vụ từ **100.000 đến 1.000.000 người dùng đồng thời**.

### 🎯 Mục tiêu

| Metric | 100K Users | 1M Users |
|--------|------------|----------|
| **Requests/sec** | 10,000 | 100,000 |
| **Response Time** | < 200ms | < 500ms |
| **Availability** | 99.99% | 99.999% |

---

## 1. Kiến trúc tổng thể

```
┌─────────────────────────────────────────────────────────────────────────────────────────┐
│                    BOOKSTORE ARCHITECTURE                                               │
├─────────────────────────────────────────────────────────────────────────────────────────┤
│                                                                                         │
│  ┌─────────────────────────────────────────────────────────────────────────────────┐  │
│  │                        CLIENT LAYER                                              │  │
│  │  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐        │  │
│  │  │  Mobile  │  │   Web    │  │  Mobile  │  │   API    │  │   CDN    │        │  │
│  │  │   App    │  │  Browser │  │   Web    │  │   Client │  │ (Static) │        │  │
│  │  └────┬─────┘  └────┬─────┘  └────┬─────┘  └────┬─────┘  └────┬─────┘        │  │
│  └───────┼─────────────┼─────────────┼─────────────┼─────────────┼───────────────┘  │
│          │             │             │             │             │                     │
│          └─────────────┴─────────────┼─────────────┴─────────────┘                     │
│                                      │                                                 │
│                                      ▼                                                 │
│  ┌─────────────────────────────────────────────────────────────────────────────────┐  │
│  │                        LOAD BALANCER (HAProxy / NGINX)                          │  │
│  │  ┌─────────────────────────────────────────────────────────────────────────┐  │  │
│  │  │  Round Robin / Least Connections / IP Hash                              │  │  │
│  │  └─────────────────────────────────────────────────────────────────────────┘  │  │
│  └────────────────────────────────────┬────────────────────────────────────────────┘  │
│                                       │                                                │
│                                       ▼                                                │
│  ┌─────────────────────────────────────────────────────────────────────────────────┐  │
│  │                        APPLICATION LAYER (Spring Boot)                         │  │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐           │  │
│  │  │  Instance 1 │  │  Instance 2 │  │  Instance 3 │  │  Instance N │           │  │
│  │  │  (Pod 1)    │  │  (Pod 2)    │  │  (Pod 3)    │  │  (Pod N)    │           │  │
│  │  └─────────────┘  └─────────────┘  └─────────────┘  └─────────────┘           │  │
│  │  🔄 Auto-scaling: CPU > 70% → Scale up                                          │  │
│  └────────────────────────────────────┬────────────────────────────────────────────┘  │
│                                       │                                                │
│          ┌────────────────────────────┼────────────────────────────┐                  │
│          │                            │                            │                  │
│          ▼                            ▼                            ▼                  │
│  ┌───────────────┐          ┌───────────────┐          ┌───────────────┐            │
│  │   CACHE       │          │   DATABASE    │          │   MESSAGE     │            │
│  │   LAYER       │          │   LAYER       │          │   QUEUE       │            │
│  │  (Redis)      │          │  (MySQL)      │          │  (RabbitMQ)   │            │
│  └───────────────┘          └───────────────┘          └───────────────┘            │
│                                                                                         │
└─────────────────────────────────────────────────────────────────────────────────────────┘
```

---

## 2. Các thành phần chính

| Layer | Công nghệ | Mục đích |
|-------|-----------|----------|
| **Client** | Web, Mobile, API | Giao diện người dùng |
| **Load Balancer** | HAProxy / NGINX | Phân phối traffic |
| **Application** | Spring Boot (Java 17) | Business logic |
| **Cache** | Redis Cluster | Tăng hiệu năng đọc |
| **Database** | MySQL (Master-Replica) | Lưu trữ dữ liệu |
| **Message Queue** | RabbitMQ | Xử lý bất đồng bộ |
| **Monitoring** | Prometheus + Grafana | Giám sát hệ thống |

---

## 3. Scaling Strategy

### 3.1. 100,000 Users

| Thành phần | Cấu hình | Số lượng |
|------------|----------|----------|
| **App Pods** | 2 cores, 4GB RAM | 80-100 |
| **Redis** | 4GB RAM per node | 6 nodes |
| **MySQL** | 8 cores, 32GB RAM | 1 Master + 3 Replicas |
| **RabbitMQ** | 4GB RAM per node | 3 nodes |
| **Load Balancer** | 4 cores, 8GB RAM | 2 nodes |

### 3.2. 1,000,000 Users

| Thành phần | Cấu hình | Số lượng |
|------------|----------|----------|
| **App Pods** | 2 cores, 4GB RAM | 800-1,000 |
| **Redis** | 8GB RAM per node | 50+ nodes |
| **MySQL Shards** | 16 cores, 64GB RAM | 10 shards + 30 replicas |
| **Kafka** | 8 cores, 16GB RAM | 10 brokers |
| **Load Balancer** | Multi-region | 10+ nodes |

### 3.3. Auto-scaling Rules (Kubernetes HPA)

```yaml
# Horizontal Pod Autoscaler
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: bookstore-hpa
  namespace: production
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: bookstore
  minReplicas: 20
  maxReplicas: 1000
  metrics:
    - type: Resource
      resource:
        name: cpu
        target:
          type: Utilization
          averageUtilization: 70
    - type: Resource
      resource:
        name: memory
        target:
          type: Utilization
          averageUtilization: 80
    - type: Pods
      pod:
        metric:
          name: http_requests_per_second
        target:
          type: AverageValue
          averageValue: 1000
```

---

## 4. Data Flow

### 4.1. Read Flow (GET /books)

```
┌─────────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│   Client    │───▶│  Load       │───▶│  App Pod    │───▶│   Redis     │
│             │    │  Balancer   │    │             │    │   (Cache)   │
└─────────────┘    └─────────────┘    └─────────────┘    └─────────────┘
                          │                  │
                          │                  │ (Cache Miss)
                          │                  ▼
                          │           ┌─────────────┐
                          └──────────▶│   MySQL     │
                                      │  (Replica)  │
                                      └─────────────┘
```

### 4.2. Write Flow (POST /checkout)

```
┌─────────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│   Client    │───▶│  Load       │───▶│  App Pod    │───▶│   MySQL     │
│             │    │  Balancer   │    │             │    │   (Master)  │
└─────────────┘    └─────────────┘    └─────────────┘    └─────────────┘
                          │                  │
                          │                  │ (Async)
                          │                  ▼
                          │           ┌─────────────┐
                          └──────────▶│  RabbitMQ   │
                                      │   / Kafka   │
                                      └─────────────┘
```

---

## 5. Database Sharding Strategy (1M Users)

```yaml
# Database Sharding Configuration
sharding:
  enabled: true
  shards: 10
  algorithm: hash
  key: user_id
  replicas_per_shard: 3

  shard_mapping:
    - shard: 0
      master: mysql-shard-0-master
      replicas:
        - mysql-shard-0-replica-1
        - mysql-shard-0-replica-2
        - mysql-shard-0-replica-3
    - shard: 1
      master: mysql-shard-1-master
      replicas:
        - mysql-shard-1-replica-1
        - mysql-shard-1-replica-2
        - mysql-shard-1-replica-3
    # ... shard 2 to 9
```

### Routing Logic

```java
// Shard routing
public class ShardRouter {
    public String getShard(Long userId) {
        int shard = (int) (userId % 10);
        return "mysql-shard-" + shard + "-master";
    }
}
```

---

## 6. Cache Strategy

```yaml
# Redis Cluster Configuration
redis:
  cluster:
    nodes: 50
    master_nodes: 25
    replica_nodes: 25
    memory_per_node: 8GB
    total_memory: 400GB
    eviction_policy: allkeys-lru

  cache_ttl:
    book_detail: 600s      # 10 phút
    category_list: 300s    # 5 phút
    homepage_featured: 60s # 1 phút
    user_session: 1800s    # 30 phút
    cart: 1800s            # 30 phút

  cache_hit_rate_target: 95%
```

### Cache Layers

```
┌─────────────────────────────────────────────────────────────────────────────────────────┐
│                    CACHE LAYERS                                                         │
├─────────────────────────────────────────────────────────────────────────────────────────┤
│                                                                                         │
│  L1: Local Cache (Caffeine)                                                            │
│  └── TTL: 1 phút, Size: 10,000 entries                                                │
│                                                                                         │
│  L2: Redis Cluster                                                                      │
│  └── TTL: 10 phút, Size: 400GB                                                        │
│                                                                                         │
│  L3: CDN (CloudFlare / CloudFront)                                                     │
│  └── TTL: 1 giờ, Static assets only                                                   │
│                                                                                         │
└─────────────────────────────────────────────────────────────────────────────────────────┘
```

---

## 7. Message Queue Strategy

### 7.1. RabbitMQ (100K Users)

```yaml
# RabbitMQ Configuration
rabbitmq:
  cluster:
    nodes: 3
    memory_per_node: 4GB
    disk_per_node: 50GB
    high_availability: mirrored

  queues:
    order_placed:
      durable: true
      max_priority: 10
    order_email:
      durable: true
    order_inventory:
      durable: true
    order_loyalty:
      durable: true

  dead_letter_queue:
    enabled: true
    max_retries: 3
```

### 7.2. Kafka (1M Users)

```yaml
# Kafka Configuration
kafka:
  cluster:
    brokers: 10
    memory_per_broker: 16GB
    disk_per_broker: 500GB
    replication_factor: 3

  topics:
    order_placed:
      partitions: 20
      retention: 7d
    order_email:
      partitions: 10
      retention: 3d
    order_inventory:
      partitions: 10
      retention: 7d
    order_loyalty:
      partitions: 10
      retention: 7d

  consumer_groups:
    email_consumer:
      max_retries: 3
      dead_letter_topic: order_email_dlq
```

---

## 8. Monitoring & Observability

### 8.1. Metrics

```yaml
# Prometheus Configuration
prometheus:
  scrape_interval: 15s
  evaluation_interval: 15s

  targets:
    - spring-actuator: /actuator/prometheus
    - node-exporter: /metrics
    - redis-exporter: /metrics
    - mysql-exporter: /metrics
    - rabbitmq-exporter: /metrics
```

### 8.2. Alerting Rules

```yaml
# Alerting Rules
groups:
  - name: bookstore-alerts
    rules:
      - alert: HighErrorRate
        expr: rate(http_requests_total{status=~"5.."}[5m]) > 0.05
        for: 5m
        labels:
          severity: critical
        annotations:
          summary: "High error rate detected"
          description: "Error rate is {{ $value }}%"

      - alert: RedisMemoryHigh
        expr: redis_memory_used_bytes / redis_memory_max_bytes > 0.85
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "Redis memory usage > 85%"
          description: "Redis memory usage: {{ $value }}%"

      - alert: DatabaseConnectionsHigh
        expr: mysql_global_status_threads_connected > 80
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "Database connections > 80%"
          description: "Connections: {{ $value }}"

      - alert: QueueDepthHigh
        expr: rabbitmq_queue_messages{queue="order_placed"} > 1000
        for: 10m
        labels:
          severity: warning
        annotations:
          summary: "Queue depth > 1000"
          description: "Queue depth: {{ $value }}"
```

---

## 9. Deployment Strategy

### 9.1. CI/CD Pipeline (GitHub Actions)

```yaml
# .github/workflows/deploy.yml
name: Deploy

on:
  push:
    branches: [ main ]
  workflow_dispatch:

jobs:
  deploy:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'

      - name: Build and Test
        run: mvn clean test jacoco:report

      - name: Build Docker Image
        run: docker build -t bookstore:latest .

      - name: Push to GHCR
        run: |
          echo ${{ secrets.GITHUB_TOKEN }} | docker login ghcr.io -u ${{ github.actor }} --password-stdin
          docker tag bookstore:latest ghcr.io/${{ github.repository }}:latest
          docker push ghcr.io/${{ github.repository }}:latest

      - name: Deploy to Kubernetes
        run: |
          kubectl set image deployment/bookstore bookstore=ghcr.io/${{ github.repository }}:latest
          kubectl rollout status deployment/bookstore
```

### 9.2. Kubernetes Deployment

```yaml
# k8s/deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: bookstore
  namespace: production
spec:
  replicas: 20
  selector:
    matchLabels:
      app: bookstore
  strategy:
    type: RollingUpdate
    rollingUpdate:
      maxSurge: 25%
      maxUnavailable: 25%
  template:
    metadata:
      labels:
        app: bookstore
    spec:
      containers:
        - name: bookstore
          image: ghcr.io/taesikwoo268/bookstore:latest
          ports:
            - containerPort: 8080
          env:
            - name: SPRING_PROFILES_ACTIVE
              value: production
          resources:
            requests:
              memory: "2Gi"
              cpu: "1000m"
            limits:
              memory: "4Gi"
              cpu: "2000m"
          livenessProbe:
            httpGet:
              path: /actuator/health/liveness
              port: 8080
            initialDelaySeconds: 30
            periodSeconds: 10
          readinessProbe:
            httpGet:
              path: /actuator/health/readiness
              port: 8080
            initialDelaySeconds: 10
            periodSeconds: 5
```

---

## 10. Disaster Recovery

```yaml
# Disaster Recovery Configuration
dr:
  rto: 15min  # Recovery Time Objective
  rpo: 5min   # Recovery Point Objective

  strategies:
    pod_failure:
      strategy: K8s auto-restart
      rto: 1min
      rpo: 0

    node_failure:
      strategy: K8s reschedule
      rto: 5min
      rpo: 0

    region_failure:
      strategy: Multi-region failover
      rto: 15min
      rpo: 5min
      backup_region: eu-west-1

    database_failure:
      strategy: Replica promotion
      rto: 10min
      rpo: 15min

    data_corruption:
      strategy: Point-in-time recovery
      rto: 30min
      rpo: 1hour
      backup_schedule: "0 */6 * * *"
```

---

## 11. Cost Estimation

### 11.1. 100K Users

```yaml
# Cost Estimation - 100K Users
cost:
  total: ~$4,700/month

  components:
    app_pods:
      count: 100
      spec: "2 cores, 4GB RAM"
      monthly: $2,500
    redis:
      count: 6
      spec: "4GB RAM"
      monthly: $500
    mysql:
      count: 4
      spec: "8 cores, 32GB RAM"
      monthly: $1,200
    rabbitmq:
      count: 3
      spec: "4GB RAM"
      monthly: $300
    load_balancer:
      count: 2
      spec: "4 cores, 8GB RAM"
      monthly: $200
```

### 11.2. 1M Users

```yaml
# Cost Estimation - 1M Users
cost:
  total: ~$51,000/month

  components:
    app_pods:
      count: 1000
      spec: "2 cores, 4GB RAM"
      monthly: $25,000
    redis:
      count: 50
      spec: "8GB RAM"
      monthly: $5,000
    mysql:
      count: 40
      spec: "16 cores, 64GB RAM"
      monthly: $15,000
    kafka:
      count: 10
      spec: "8 cores, 16GB RAM"
      monthly: $3,000
    load_balancer:
      count: 10
      spec: "multi-region"
      monthly: $2,000
    cdn:
      count: 1
      monthly: $1,000
```

---

## 12. Roadmap

```yaml
# Roadmap
roadmap:
  phase_1_mvp:
    duration: "Current"
    features:
      - Spring Boot + MySQL
      - Redis caching
      - Docker + Docker Compose
      - Basic monitoring

  phase_2_100k:
    duration: "3-6 months"
    features:
      - Kubernetes cluster
      - Auto-scaling (HPA)
      - RabbitMQ
      - CI/CD (GitHub Actions)
      - Prometheus + Grafana
      - ELK Stack logging
      - Load testing (k6)

  phase_3_1m:
    duration: "6-12 months"
    features:
      - Database sharding (10 shards)
      - Kafka migration
      - Multi-region deployment
      - Service Mesh (Istio)
      - APM + Distributed Tracing (Jaeger)
      - Chaos Engineering
      - Security hardening (WAF, DDoS)
```

---

## 📝 Kết luận

BookStore được thiết kế để **scale từ 100K lên 1M users** với các chiến lược:

1. ✅ **Horizontal scaling** (thêm Pods)
2. ✅ **Caching** (Redis cluster, 95% hit rate)
3. ✅ **Database sharding** (10+ shards)
4. ✅ **Async processing** (RabbitMQ → Kafka)
5. ✅ **Multi-region** (geo-distribution)
6. ✅ **Auto-scaling** (CPU + event-based)
7. ✅ **Observability** (Metrics + Logging + Tracing)

**🚀 Sẵn sàng cho 1M users!**