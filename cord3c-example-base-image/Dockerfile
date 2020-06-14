FROM google/cloud-sdk:271.0.0-slim as build
# build args
ARG JDK_VERSION
ARG JDK_FILENAME
ARG HELM_VERSION
ARG HELM_FILENAME
ARG GRADLE_VERSION
ARG GRADLE_FILENAME
ARG KUBERNETES_VERSION
ARG DOCKER_VERSION
ARG DOCTL_VERSION
ARG TF_VERSION
ARG TF_FILENAME
# proxy flag
RUN if [ -n $HTTPS_PROXY ]; then export PROXY_PARAM="-x HTTPS_PROXY=${HTTPS_PROXY}"; else export PROXY_PARAM=""; fi
WORKDIR /
RUN apt-get -qqy update && apt-get install -qqy unzip
# should be at the begining because the extracted folder name cannot be parameterized -> /tmp has not used at this point
RUN curl "${PROXY_PARAM}" -L https://download.docker.com/linux/static/stable/x86_64/docker-${DOCKER_VERSION}.tgz > docker-${DOCKER_VERSION}.tgz && \
	tar zxv -C /tmp -f docker-${DOCKER_VERSION}.tgz && \
	chmod a+x /tmp/docker && \
	rm -f docker-${DOCKER_VERSION}.tgz
RUN curl "${PROXY_PARAM}" -L https://github.com/AdoptOpenJDK/openjdk8-binaries/releases/download/jdk${JDK_VERSION}/${JDK_FILENAME} > ${JDK_FILENAME} && \
	tar zxv -C /tmp -f ${JDK_FILENAME} && \
	mv /tmp/jdk${JDK_VERSION}/ /tmp/jdk/ && \
	chmod -R a+x /tmp/jdk/bin && \
	rm -f ${JDK_FILENAME}
RUN curl "${PROXY_PARAM}" -L https://services.gradle.org/distributions/${GRADLE_FILENAME} > ${GRADLE_FILENAME} && \
    unzip -d /opt ${GRADLE_FILENAME} && \
    mv /opt/gradle-${GRADLE_VERSION}/ /opt/gradle/ && \
    chmod -R a+x /opt/gradle && \
    rm -f ${GRADLE_FILENAME}
RUN curl "${PROXY_PARAM}" -L https://get.helm.sh/${HELM_FILENAME} > ${HELM_FILENAME} && \
    tar zxv -C /tmp -f ${HELM_FILENAME} && \
    chmod a+x /tmp/linux-amd64/helm && \
    rm -f ${HELM_FILENAME}
RUN curl -L0 https://storage.googleapis.com/kubernetes-release/release/${KUBERNETES_VERSION}/bin/linux/amd64/kubectl > /bin/kubectl && \
	chmod a+x /bin/kubectl


RUN curl "${PROXY_PARAM}" -L https://github.com/digitalocean/doctl/releases/download/v${DOCTL_VERSION}/doctl-${DOCTL_VERSION}-linux-amd64.tar.gz > doctl.tar.gz && \
    tar zxv -C /tmp -f doctl.tar.gz && \
    chmod a+x /tmp/doctl && \
    rm -f doctl.tar.gz





RUN curl -L https://releases.hashicorp.com/terraform/${TF_VERSION}/${TF_FILENAME} > ${TF_FILENAME} && \
	mkdir /tmp/tf && \
	unzip -d /tmp/tf terraform_${TF_VERSION}_linux_amd64.zip && \
	mv /tmp/tf/terraform /usr/local/bin/


FROM google/cloud-sdk:271.0.0-slim
COPY --from=build /tmp/docker/* /bin/
COPY --from=build /tmp/doctl /bin/
COPY --from=build /tmp/jdk/ /usr/lib/jvm/java-8-openjdk-amd64/
COPY --from=build /tmp/linux-amd64/helm /bin/helm
COPY --from=build /opt/gradle /opt/gradle
COPY --from=build /bin/kubectl /bin/kubectl
COPY --from=build /usr/local/bin/terraform /usr/local/bin/terraform
RUN ln -s /opt/gradle/bin/* /usr/bin/ && \
	ln -s /usr/lib/jvm/java-8-openjdk-amd64/bin/* /usr/bin/
RUN apt-get -y update
RUN apt-get -y install fontconfig
