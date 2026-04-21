#!/bin/bash
# Copyright (c) 2022 Oracle and/or its affiliates.
# Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl.

# Make sure this is run via source or .
if ! (return 0 2>/dev/null); then
  echo "ERROR: Usage 'source env.sh'"
  exit
fi

# POSIX compliant find and replace
function sed_i(){
  local OP="$1"
  local FILE="$2"
  sed -e "$OP" "$FILE" >"/tmp/$FILE"
  mv -- "/tmp/$FILE" "$FILE"
}
export -f sed_i

# Java Home
function set_javahome(){
  if [[ "$OSTYPE" == "darwin"* ]]; then
    # MacOS
    export JAVA_HOME=$(/usr/libexec/java_home -v 17 2>/dev/null || /usr/libexec/java_home)
  else
    # Linux/Others - keep current logic if directory exists
    if test -d ~/graalvm-community-openjdk-22.0.2+9.1/bin; then
      export JAVA_HOME=~/graalvm-community-openjdk-22.0.2+9.1
    fi
  fi
  export PATH=$JAVA_HOME/bin:$PATH
}

#set mtdrworkshop_location
if [ -n "$BASH_VERSION" ]; then
    DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"
elif [ -n "$ZSH_VERSION" ]; then
    DIR="$( cd "$( dirname "${(%):-%x}" )" &> /dev/null && pwd )"
else
    DIR="$( cd "$( dirname "$0" )" &> /dev/null && pwd )"
fi
export MTDRWORKSHOP_LOCATION="$DIR"
echo "MTDRWORKSHOP_LOCATION: $MTDRWORKSHOP_LOCATION"



JAVA_TEST=$(java -version 2>&1)
if echo "$JAVA_TEST" | grep -q "22\."; then
  echo "JAVA Found: $JAVA_TEST"
else
  set_javahome
fi

#state directory
if test -d ~/mtdrworkshop-state; then
  export MTDRWORKSHOP_STATE_HOME=~/mtdrworkshop-state
else
  export MTDRWORKSHOP_STATE_HOME=$MTDRWORKSHOP_LOCATION
fi
echo "MTDRWORKSOP_STATE_HOME: $MTDRWORKSHOP_STATE_HOME"
#Log Directory
export MTDRWORKSHOP_LOG=$MTDRWORKSHOP_STATE_HOME/log
mkdir -p $MTDRWORKSHOP_LOG

source $MTDRWORKSHOP_LOCATION/utils/state-functions.sh

# SHORTCUT ALIASES AND UTILS...
alias k='kubectl'
alias kt='kubectl --insecure-skip-tls-verify'
alias pods='kubectl get po --all-namespaces'
alias services='kubectl get services --all-namespaces'
alias gateways='kubectl get gateways --all-namespaces'
alias secrets='kubectl get secrets --all-namespaces'
alias ingresssecret='kubectl get secrets --all-namespaces | grep istio-ingressgateway-certs'
alias virtualservices='kubectl get virtualservices --all-namespaces'
alias deployments='kubectl get deployments --all-namespaces'
alias mtdrworkshop='echo deployments... ; deployments|grep mtdrworkshop ; echo pods... ; pods|grep mtdrworkshop ; echo services... ; services | grep mtdrworkshop ; echo secrets... ; secrets|grep mtdrworkshop ; echo "other shortcut commands... most can take partial podname as argument, such as [logpod front] or [deletepod order]...  pods  services secrets deployments " ; ls $MTDRWORKSHOP_LOCATION/utils/'
alias sshpod1='kubectl exec -i -t $(kubectl get pod --namespace mtdrworkshop --selector="app=hud" --output jsonpath="{.items[0].metadata.name}") -n mtdrworkshop -- /bin/bash'


export PATH=$PATH:$MTDRWORKSHOP_LOCATION/utils/

# OCI Configuration
export OCI_USER_OCID="ocid1.user.oc1..aaaaaaaaidyv7v7atn4dauxulsktv636wnbu5t2h4ibogrbosiim5fkadlmq"
export TEST_USER_OCID=$OCI_USER_OCID
