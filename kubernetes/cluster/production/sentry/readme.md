helm install --name sentry -f values.yaml --wait stable/sentry --namespace sentry
helm upgrade -i sentry -f values.yaml --wait stable/sentry --timeout=900 --namespace sentry
