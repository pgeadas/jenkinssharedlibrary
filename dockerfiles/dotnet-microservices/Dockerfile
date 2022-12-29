FROM pomma89/dotnet-mono:dotnet-3-mono-6-sdk as base

WORKDIR /root

COPY .paket/ ./.paket/
COPY paket.dependencies .
COPY paket.lock .
RUN mono ./.paket/paket.exe restore

FROM base as builder
COPY fsc.props .
COPY Common/ ./Common/
COPY ServiceName/ ./ServiceName/

FROM builder as publisher
WORKDIR /root/ServiceName
RUN dotnet publish -o published -c Release

FROM mcr.microsoft.com/dotnet/core/aspnet:3.0.0-alpine3.9 as production
WORKDIR /root/
COPY --from=publisher /root/ServiceName/published /root
EXPOSE 9999
CMD ["dotnet", "./ServiceName.dll"]

FROM builder as test
COPY ServiceName.Test/* /root/ServiceName.Test/
WORKDIR /root/ServiceName.Test

RUN  dotnet test /root/ServiceName.Test  --logger:"trx;LogFileName=unit_tests.xml"

