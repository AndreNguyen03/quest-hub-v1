.PHONY: run build vet test tidy swagger

run:
	go run .

build:
	go build -o bin/notification.exe .

vet:
	go vet ./...

test:
	go test ./...

tidy:
	go mod tidy

swagger:
	swag init -g main.go --parseDependency --parseInternal
