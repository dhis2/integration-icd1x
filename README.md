# Integration for ICD 10/11 to DHIS2

## Requirements

* Java 11+
* Docker(Optional), if using the local ICD API
* ICD API keys(optional), if using the remote ICD API

## ICD API

Refer [WHO ICD Docs](https://icd.who.int/icdapi) for information about accessing ICD-API remotely or locally.

**Note:** Only ICD11 is available in docker mode(locally). ICD10 codes should always be fetched from the remote API.

## Building & Starting

```bash
sh ./start.sh
```

## Commands

### icd11

```bash
NAME
	icd11 - Generate DHIS2 OptionsSet with ICD11 codes and saves the output to a file

SYNOPSYS
	icd11 [[--root-id] string]  [[--release-id] string]  [[--linearization-name] string]  [[--languages] set]  [[--host] string]  [[--client-id] string]  [[--client-secret] string]  [[--file-out] string]  [--verbose]  

OPTIONS
	--root-id  string
		ICD Entity ID to start with
		[Optional, default = ]

	--release-id  string
		ICD 11 Release Id. One of 2021-05, 2020-09, 2019-04, 2018
		[Optional, default = 2021-05]

	--linearization-name  string
		Short name for the linearization. e.g. mms for ICD Mortality and Morbidity Statistics
		[Optional, default = mms]

	--languages  set
		Language for entity descriptions. A set of ar, en, es, zh
		[Optional, default = en]

	--host  string
		Host of the ICD11 repository. The default value works with docker approach
		[Optional, default = http://localhost]

	--client-id  string
		The client id to be used with the publicly hosted icd1 repository
		[Optional, default = ]

	--client-secret  string
		The client secret to be used with the publicly hosted icd1 repository
		[Optional, default = ]

	--file-out  string
		Path to the output
		[Optional, default = output]

	--verbose	Indicates whether progress should be displayed verbosely
		[Optional, default = false]
```

### icd10

```bash
NAME
	icd10 - Generate DHIS2 OptionsSet with ICD10 codes and saves the output to a file

SYNOPSYS
	icd10 [[--release-id] string]  [[--root-category] string]  [[--languages] set]  [[--client-id] string]  [[--client-secret] string]  [[--file-out] string]  [--verbose]  

OPTIONS
	--release-id  string
		ICD 10 Release Id. One of 2008, 2010, 2016, 2019
		[Optional, default = 2016]

	--root-category  string
		ICD category code to start with
		[Optional, default = ]

	--languages  set
		Language for entity descriptions. A set of ar, en, es, zh
		[Optional, default = en]

	--client-id  string
		The client id to be used with the publicly hosted icd1 repository
		[Optional, default = ]

	--client-secret  string
		The client secret to be used with the publicly hosted icd1 repository
		[Optional, default = ]

	--file-out  string
		Path to the output
		[Optional, default = output]

	--verbose	Indicates whether progress should be displayed verbosely
		[Optional, default = false]
```

## Adding Codes to DHIS2

The resulting file can be imported into DHIS2 via the Metadata import option of the Import/Export App.