
# Apache Hop RScript
Apache Hop R  Integration Plugin is used to execute R code direct from Apache Hop (Hop). This plugin is overwritten from the pentaho original created by *dekarlab*.

## Building
The **Hop RScript Plugin** is built with Maven.
 ```sh
    $ git clone https://github.com/ravamo/hop-rscript.git
    $ cd hop-rscript
    $ mvn clean install
 ```
This will produce a plugin archive in target/hop-rscript-${version}.zip. This archive can then be extracted into your Hop plugin/transforms directory.

## Instalation in Apache hop

R script plugin can be installed by performing the following steps:
1. Copy the plugin folder (RScriptPlugin) into the folder of your Hop installation: apache-hop-client-{version}\hop\plugins\transforms
2. Install R project from the site http://www.r-project.org/
3.  Install rJava package in R by executing: install.packages("rJava")
4. Copy JRI library rJava/jri/jri.dll (windows) or  rJava/jri/libjri.so (linux) to: data-integration/libswt/**{youroperation system}**, for example: data-integration/libswt/win64
5. Specify location of R using R_HOME environment variable. (like:   C:\R\R-3.0.2).
6. For windows, it is also needed to put in PATH variable the path to R.dll, for example: C:\R\R-{your vresion}\bin\{your operation system}

## Roadmap

- [x] Migrate the functionality to PDI to Apache Hop
- [ ] Add the posibility to run a snippet code
- [ ] Fix the old bug to pdi [pentahor](https://github.com/dekarlab/pentahor/issues) 
