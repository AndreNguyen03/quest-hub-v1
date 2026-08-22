---
name: enterprise-classical-ai-ml
description: Production architecture and engineering rules for classical machine learning systems, including data pipelines, feature engineering, training, evaluation, model registry, model serving, monitoring, retraining, and MLOps.
---

# Enterprise Classical AI / ML Skill

## 1. Purpose

Use this skill when designing, implementing, reviewing, or operating classical machine-learning systems such as:

- classification
- regression
- ranking
- recommendation
- forecasting
- anomaly detection
- computer vision
- tabular prediction

The core lifecycle is:

```text
Problem
→ Data
→ Validation
→ Feature Engineering
→ Training
→ Evaluation
→ Experiment Tracking
→ Model Artifact / Registry
→ Packaging
→ Deployment
→ Monitoring
→ Retraining
→ Versioning
→ Rollback
```

Do not assume every project needs every stage or every platform component.

The architecture must follow actual requirements for scale, latency, data freshness, model complexity, compliance, team ownership, and operating cost.

## 2. Architecture Principles

### 2.1 Separate data, training, serving, and operations

Do not automatically put:

- data processing
- model training
- evaluation
- online inference
- orchestration

into one runtime or dependency environment.

Training and serving usually have materially different resource, security, release, and latency requirements.

### 2.2 Training-serving parity

The same feature definitions and transformations must produce semantically compatible inputs during training and inference.

Avoid:

```text
training preprocessing ≠ production preprocessing
```

because this creates training-serving skew.

### 2.3 Reproducibility

A production model should be traceable to:

```text
code version
+
data version
+
feature/configuration version
+
model artifact
+
training metadata
```

The exact tools are implementation choices.

### 2.4 Dependency isolation

Training environments may contain heavy dependencies such as:

```text
deep-learning frameworks
distributed-compute frameworks
data-processing libraries
experiment tracking clients
```

Serving environments should contain only runtime dependencies actually required for inference.

This does not require separate Git repositories in every organization. It requires clear runtime/deployment boundaries.

## 3. Recommended Repository Boundary

A production system can use separate deployable units such as:

```text
ml-training/
ml-serving/
```

or a repository containing clearly separated applications.

Choose repository separation when independent:

- dependency graphs
- release cycles
- compute profiles
- ownership
- security boundaries

justify it.

Do not split repositories merely to create visual separation.

## 4. Project Structure

A practical standalone training project:

```text
ml-training/
├── configs/
│   ├── base.yaml
│   ├── data.yaml
│   ├── training.yaml
│   └── evaluation.yaml
├── src/
│   └── ml_project/
│       ├── data/
│       │   ├── ingestion.py
│       │   ├── validation.py
│       │   └── transformations.py
│       ├── features/
│       │   ├── definitions.py
│       │   └── pipelines.py
│       ├── training/
│       │   ├── trainer.py
│       │   ├── datasets.py
│       │   └── hyperparameters.py
│       ├── evaluation/
│       │   ├── metrics.py
│       │   ├── validation.py
│       │   └── reports.py
│       ├── registry/
│       │   └── client.py
│       └── pipelines/
│           └── orchestration.py
├── tests/
│   ├── unit/
│   ├── integration/
│   └── e2e/
├── pyproject.toml
└── README.md
```

A serving project may be independently deployed:

```text
ml-serving/
├── src/
│   └── model_service/
│       ├── api/
│       ├── schemas/
│       ├── predictor/
│       ├── model_loader/
│       ├── feature_access/
│       ├── health/
│       └── telemetry/
├── tests/
│   ├── unit/
│   ├── integration/
│   └── e2e/
├── pyproject.toml
├── Dockerfile
└── README.md
```

Use only the directories required by the actual system.

## 5. Data Layer

Data engineering responsibilities should include:

- ingestion
- schema validation
- missing-value handling
- type validation
- distribution validation
- deduplication
- data-quality checks
- lineage where required

Treat datasets and transformations as versioned production inputs.

Do not let notebooks become the only source of truth for production transformations.

## 6. Feature Engineering

Feature engineering may include:

- extraction
- normalization
- aggregation
- temporal transformations
- categorical encoding
- embeddings where relevant

A Feature Store is useful when the organization has meaningful requirements for:

- feature reuse
- offline/online consistency
- low-latency online features
- centralized governance
- point-in-time correctness
- multiple models/teams

A Feature Store is not mandatory for every ML application.

## 7. Training

Training code should make the following explicit:

- dataset selection
- split strategy
- preprocessing
- model configuration
- random seeds where applicable
- hyperparameters
- training outputs
- artifact location
- evaluation outputs

Training pipelines should be repeatable from clean environments.

## 8. Evaluation

Separate:

### Model quality

Examples:

```text
Precision
Recall
F1
ROC-AUC
RMSE
MAE
MAP@K
NDCG
```

### Data quality

Examples:

```text
schema validity
missingness
distribution checks
label quality
leakage checks
```

### Operational quality

Examples:

```text
latency
throughput
memory usage
startup time
resource utilization
```

Do not use one metric as the universal definition of model quality.

## 9. Experiment Tracking

Record enough metadata to reproduce and compare experiments:

- code version
- dataset version
- model configuration
- hyperparameters
- metrics
- artifacts
- environment information

Tools such as MLflow or W&B are implementation choices.

## 10. Model Registry

Use a registry when model lifecycle management requires:

- versioned artifacts
- lineage
- metadata
- approval/selection workflows
- deployment references

Prefer modern version/alias/tag semantics supported by the chosen registry instead of assuming a universal `Staging → Production → Archived` lifecycle.

## 11. Model Packaging

Choose the serving representation according to runtime requirements.

Examples:

```text
native framework artifact
ONNX
TensorRT
compiled/runtime-specific artifact
```

Do not convert models merely because a particular format is fashionable.

Packaging must guarantee:

- reproducibility
- compatible preprocessing
- deterministic configuration
- required runtime libraries
- health/readiness behavior

## 12. Serving Architecture

Typical real-time path:

```text
Client
  ↓
API / Inference Gateway
  ↓
Model Serving Runtime
  ↓
Feature Access / Preprocessing
  ↓
Model
  ↓
Prediction Response
```

Typical batch path:

```text
Data Source
  ↓
Batch Inference Job
  ↓
Predictions
  ↓
Storage / Downstream Systems
```

Use the simplest serving architecture that satisfies latency, throughput, availability, and scaling requirements.

## 13. Online vs Batch Inference

Use online inference when:

- prediction is required during a request
- low latency matters
- the consumer expects immediate results

Use batch inference when:

- predictions can be precomputed
- large datasets must be processed efficiently
- latency requirements are relaxed

Do not force online serving for inherently batch workloads.

## 14. Monitoring

Separate system monitoring from model monitoring.

### System monitoring

Track:

- request latency
- throughput
- error rate
- CPU/GPU utilization
- memory
- saturation
- availability

### ML monitoring

Track where labels or reference data become available:

- feature distribution
- data drift
- prediction distribution
- model performance
- concept/performance degradation
- training-serving skew

Do not assume drift automatically means retraining is required.

## 15. Retraining

Retraining may be:

```text
scheduled
event-driven
manually approved
performance-triggered
```

A drift signal should not blindly trigger retraining.

A retraining workflow should include:

```text
trigger
→ dataset creation
→ validation
→ training
→ evaluation
→ approval
→ registry
→ deployment
→ monitoring
```

## 16. CI/CD and MLOps

CI should validate:

- source code
- tests
- schemas/contracts
- linting/type checking
- small smoke training where useful

CD should promote immutable artifacts through environments.

MLOps orchestration may use workflow engines when operational complexity justifies them.

Do not introduce distributed orchestration for a small scheduled training job without a real need.

## 17. Testing

Use:

```text
Unit
Integration
End-to-End
Data validation
Training smoke tests
Model validation
```

### Unit

Test pure transformations, feature calculations, metrics, and business rules.

### Integration

Test interactions with real or controlled data stores, feature stores, model registries, and services.

### E2E

Test production-like training/deployment workflows.

### Data tests

Detect schema changes, invalid values, leakage, and unexpected distribution changes.

Do not mock every external dependency blindly; test critical integrations realistically.

## 18. Security and Governance

Protect:

- training data
- model artifacts
- credentials
- registry access
- inference endpoints
- personally identifiable information

Apply:

- least privilege
- secret management
- artifact access controls
- audit logging where required
- dependency and image scanning
- model/data lineage

## 19. Observability

Record enough context to correlate:

```text
request
→ model version
→ feature/config version
→ prediction
→ latency
→ outcome
```

Do not put observability logic directly into core model mathematics.

## 20. Versioning

Version independently but correlate:

```text
Git commit
Dataset
Features / transformation configuration
Model artifact
Serving configuration
```

A deployed model must be traceable to its complete production lineage.

## 21. Rollback

Rollback must support returning to a previously validated model artifact and compatible configuration.

Possible mechanisms:

- previous deployment artifact
- traffic switching
- model alias/version reassignment
- batch job version selection

Rollback must preserve feature/preprocessing compatibility.

## 22. Architecture Evolution

### Early stage

```text
simple training pipeline
+
simple batch/HTTP serving
```

### Growing system

Introduce:

- stronger data validation
- model registry
- reproducible builds
- dedicated serving runtime
- automated monitoring

### Enterprise platform

Introduce only when justified:

- feature platform
- distributed training
- workflow orchestration
- centralized model governance
- self-service inference
- multi-team platform capabilities

Do not start with the platform before the workload requires it.

## 23. Anti-Patterns

Avoid:

- training code inside the online API process
- serving containers carrying unnecessary training dependencies
- notebook-only production logic
- unversioned datasets
- untracked model artifacts
- inconsistent preprocessing
- blind retraining from drift alerts
- Feature Store by default
- distributed infrastructure without workload justification
- mixing experimentation and production runtime dependencies

## 24. Decision Procedure

When reviewing a classical ML system:

1. Define the business prediction problem and success metrics.
2. Identify data sources and data-quality contracts.
3. Establish reproducible feature transformations.
4. Define training/evaluation boundaries.
5. Version data, code, configuration, and artifacts.
6. Choose batch or online inference.
7. Separate training and serving runtimes when justified.
8. Add registry/governance according to operational needs.
9. Monitor both system health and model behavior.
10. Define retraining and rollback criteria.
11. Scale infrastructure only when measurements justify it.

## 25. Non-Goals

This skill does not mandate:

- a specific ML framework
- a Feature Store
- Kubernetes
- MLflow
- Triton
- Spark
- Ray
- a specific cloud
- microservices
- separate repositories in every project
