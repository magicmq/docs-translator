[![Sonatype Nexus (Releases)](https://img.shields.io/nexus/r/dev.magicmq/docs-translator?nexusVersion=3&server=https%3A%2F%2Frepo.magicmq.dev&label=Latest%20Release)](https://repo.magicmq.dev/#browse/browse:maven-releases:dev%2Fmagicmq%2Fdocs-translator)
[![Latest Snapshot](https://img.shields.io/badge/dynamic/xml?color=orange&label=Latest%20Snapshot&query=%2F%2Fmetadata%2Fversioning%2Fversions%2Fversion%5Blast()%5D&url=https%3A%2F%2Frepo.magicmq.dev%2Frepository%2Fmaven-snapshots%2Fdev%2Fmagicmq%2Fdocs-translator%2Fmaven-metadata.xml)](https://ci.magicmq.dev/job/DocsTranslator/)
![GitHub Workflow Status](https://img.shields.io/github/actions/workflow/status/magicmq/docs-translator/maven.yml?branch=master)
![Apache 2.0 License](https://img.shields.io/github/license/magicmq/docs-translator)

# DocsTranslator

DocsTranslator is a Java application that attempts to translate documented Java source code (a `*-sources.jar` file generated when building a project) into  documented Python code that can be imported and utilized in Python scripts, with documentation of available classes/methods visible in the Python IDE.

## Rationale

This project generates Python source files that are intended to be used in conjunction with [PySpigot](https://github.com/magicmq/pyspigot), a Python scripting engine that works within Minecraft. PySpigot utilizes Jython, which is a Python implementation that runs on the JVM. More specifically, this project allows for autocomplete, code suggestions, and documentation usage when writing Python scripts that utilize Java classes.

PySpigot scripts are able to access Java classes at runtime, but one issue is the lack of autocomplete and code suggestions when writing scripts, as none of these Java classes are available when writing Python.

Therefore, the objective of this project is to translate those Java classes (as well as their accompanying documentation) into readable Python source code, so that autocomplete, code suggestions, and documentation are available when writing PySpigot/Jython scripts.

## How It Works

DocsTranslator relies heavily upon the [JavaParser](https://javaparser.org/) library, which, in the most simple terms, reads Java source (`.java`) files and turns them into an [abstract syntax tree](https://en.wikipedia.org/wiki/Abstract_syntax_tree), which can be read programmatically and translated into Python source files with relative ease.

The application runs in a stepwise fashion:

1. The application initializes all working directories.
2. Java source JAR files (I.E. those that follow the format `*-sources.jar`) are fetched from remote Maven repositories (for artifacts defined in the `settings.yml`) and installed into a local Maven repository using [Apache Maven Resolver](https://maven.apache.org/resolver/).
3. Apache Maven Resolver resolves and fetches dependencies for all artifacts fetched in the previous step, using the scope specified in the `settings.yml`. Runtime dependencies are fetched by default, as these will be accessible at runtime.
4. The application loops through the contents of all fetched JAR files. When it encounters a Java soruce file (ending in `.java`), the file is parsed with JavaParser, and a best-effort attempt is made to translate the source file into Python code.
    * Any files not ending in `.java` are ignored.
5. Translated `.py` files are placed in the user-defined output folder (`generated` by default), in the appropriate package.
6. An entry is added to the `__init__.py` file in the appropriate package, to allow for importing the python module as one would normally import a Java class in Jython.
7. Any source files from the Java Standard Library utilized by the previously translated Java source files are also translated in the same process outlined above.
    * JDK sources must be downloaded manually and placed in the appropriate folder (`jdk-sources` by default)
    * This step is only completed if enabled in the `settings.yml` (via the `jdkSources.translate` option)
8. All `__init__.py` files are generated and placed in their appropriate locations.
9. Python package-related files (`setup.py`, `pyproject.toml`, `MANIFEST.in`, `LICENSE`) are generated from options specified in the `settings.yml` and are placed in the user-defined output folder (`generated` by default).

Generated files are intended to be built into a Python package that can subsequently be installed into a Python virtual environment and imported.

## Usage

If you found this repository, but you are looking for instructions on how to use autocomplete, code suggestions, and documentation when writing PySpigot scripts, visit [PySpigot's documentation](https://pyspigot-docs.magicmq.dev/awdawdawd).

You may use DocsTranslator to translate Java source files into documented Python code. Download the latest release from the [releases](https://github.com/magicmq/docs-translator/releases/) page. DocsTranslator is a standalone Java application, so you must run it with `java -jar docs-translator.jar`. You will likely want to modify some settings, so see the [Settings](#Settings) section below for information on the configuration.

If you encounter any issues while using DocsTranslator, [submit an issue report](https://github.com/magicmq/docs-translator/issues).

### Building DocsTranslator

Building requires [Maven](https://maven.apache.org/) and [Git](https://git-scm.com/). Maven 3+ is recommended for building the project. Follow these steps:

1. Clone the repository: `git clone https://github.com/magicmq/docs-translator.git`
2. Enter the repository root: `cd docs-translator`
3. Build with Maven: `mvn clean package`
4. Built files will be located in the `target` directory.

## Contributing

Any contributions you make to DocsTranslator are **greatly appreciated**.

If you have a suggestion or modification that would make DocsTranslator better, please [fork the repo](https://github.com/magicmq/docs-translator/fork) and create a pull request. You can also simply [open an issue](https://github.com/magicmq/docs-translator/issues/new) with the tag "enhancement".

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## Settings

The `settings.yml` file is the main configuration file for the project. If a `settings.yml` file doesn't already exist in the same directory as the DocsTranslator JAR file when it is run, then a default version is generated and placed there.

Options are outlined below.

### `maven`:

Options pertaining to fetching the JAR file (and its dependencies) to be translated. Downloaded JARs are placed into a local Maven repository.

- `path`: The path where the local Maven repository should be placed.
- `useCentral`: If set to `true`, Maven Central will be included as one of the remote repositories to search for dependencies.
- `repositories`: A list of remote repositories to be searched for the listed artifacts to translate (and its dependencies). Each item in the list should contain an `id` to identify it and a `url` pointing to the location of the remote repository.
- `deleteOnStart`: If set to `true`, the folder containing the local Maven repository will be deleted when DocsTranslator first runs.
- `artifacts`: A list of artifacts (in the format `groupId:artifactId:version`) to fetch and translate.
- `excludeArtifacts`: A list of artifacts to exclude. Useful for excluding dependency artifacts from translation.
  - Use the format `groupId:artifactId` to exclude a single artifact
  - Use the format `groupId` to exclude all artifacts under a particular group
- `dependencyScope`: The [scope](https://maven.apache.org/guides/introduction/introduction-to-dependency-mechanism.html#Dependency_Scope) to use to limit transitivity when fetching dependencies of an artifact.

### `jdkSources`:

Options pertaining to sources from the Java Standard Library.

- `translate`: If set to `true`, any utilized Java Standard Library source files will also be translated.
- `path`: The path to the folder where the Java Standard Library sources are located.
- `group`: Used when adding docstrings to the generated `.py` modules or Java Standard Library sources.
- `name`: Used when adding docstrings to the generated `.py` modules for Java Standard Library sources.
- `version`: Used when adding docstrings to the generated `.py` modules for Java Standard Library sources.

### `output`:

Options pertaining to the generated `.py` files.

- `path`: The path to the folder where generated Python sources are placed.
- `deleteOnStart`: If set to `true`, the folder containing generated `.py` files will be deleted when DocsTranslator first runs.

### `importExclusions`:

Options pertaining to excluding imports in generated `.py` files.

- `packages`: A list of packages to exclude when adding imports to generated `.py` files.
- `classes`: A list of Java classes to exclude when adding imports to generated `.py` files.

### `formats`:

Options that specify the format, structure, and syntax of generated Python code.

#### `module`:

Options for module formatting.

- `docString`: The format of the docstring placed at the top of all generated Python modules.
- `importDeclaration`: The standard format of imports.

#### `class_`:

Options for class formatting.

- `declaration`: The format of a class declaration.
- `declarationExtending`: The format of a class declaration when the class extends/implements another class.

#### `enum`:

Options for enum formatting.

- `declaration`: The format of an enum declaration.
- `entryRegular`: The format of an enum entry without any arguments.
- `entryWithArgs`: The format of an enum entry with arguments.

#### `function`:

Options for formatting of functions.

- `initDefinition`: The format of the `__init__` function for classes.
- `definition`: The format of a regular function definition.
- `parameterRegular`: The format of a regular function parameter.
- `parameterVararg`: The format of a VarArg parameter.
- `returnRegular`: The format of a function return statement if the corresponding Java method does not return anything.
- `returnWithValue`: The format of a function return statement if the corresponding Java method returns a value. Used only for translation of Java annotations (where an element may have a default value).

#### `field`:

Options for formatting of fields.

- `initializer`: The format of a field with a value. Used mainly for `static` and `final` fields.

#### `docString`:

Options for translation of JavaDoc comments into Python docstrings.

- `author`: The format for the JavaDoc `@author` tag.
- `deprecated`: The format for the JavaDoc `@deprecated` tag.
- `param`: The format for the JavaDoc `@param` tag.
- `typeParam`: The format for any defined type parameter.
- `return`: The format for the JavaDoc `@returns` tag.
- `see`: The format for the JavaDoc `@see` tag.
- `serial`: The format for the JavaDoc `@serial` tag.
- `serialData`: The format for the JavaDoc `@serialdata` tag.
- `serialField`: The format for the JavaDoc `@serialfield` tag.
- `since`: The format for the JavaDoc `@since` tag.
- `throw`: The format for the JavaDoc `@throws` tag.
- `version`: The format for the JavaDoc `@version` tag.
- `unknown`: The header format for any unknown or unparseable JavaDoc tag.
- `unknownTag`: The format for each unknown tag. 

### `packaging`:

Options for generated `setup.py` and `pyproject.toml` files.

#### `setup`:

Options for the generated `setup.py` file.

- `name`: The name of the Python package.
- `version`: The version of the Python package.
- `author`: The author of the Python package.
- `authorEmail`: The author's email.
- `description`: The description of the Python package.
- `url`: The URL pointing to the site of the Python package.
- `pyModules`: Modules to include in the final Python package that are not located within another package (with an `__init__.py` file).
- `pythonRequires`: The minimum required Python version to install the package.
- `classifiers`: Trove classifiers for the project.

#### `pyProject`:

Options for the generated `pyproject.toml` file.

- `requires`: Required modules/packages to build the project.
- `buildBackend`: The build backend for the project.

#### `manifest`:

A list of lines to include in the `MANIFEST.in` file for the Python package.

#### `license`:

A URL pointing to the license text to bundle with the Python package.

## Caveats/Known Issues

### Type Translation

Because Java is statically-typed, but Python is not, translation of Java types to Python types is not perfect. Additionally, not all Java types are seamlessly interchangeable with Python types. For example:

* The Java `Collection` type is translated to a Python `Iterable`, although these types are not wholly interchangeable.
* Generics are not fully translated. This could be attempted with the `TypeVar` class (available in Python's `typing` module), however, I did not pursue this given that it would become quite completed with Java classes that contain several generic methods.
* Python has no direct equivalent to Java's `char` type, which has consequences. For example, consider the following two overloaded methods from the `org.bukkit.ChatColor` class:
  ```java
  @Nullable
  public static ChatColor getByChar(char code) {
      ...
  }
  
  @Nullable
  public static ChatColor getByChar(@NotNull String code) {
      ...
  }
  ```
  DocsTranslator translates these to:
  ```py
  @overload
  @staticmethod
  def getByChar(code: str) -> "ChatColor":
      ...

  @overload
  @staticmethod
  def getByChar(code: str) -> "ChatColor":
      ...
  ```
  The translated Python functions are **identical**, even though they are **not** identical in Java. DocsTranslator translates `char` to `str`, and, as a consequence, these two translated functions accept the same parameters.
* Other examples of imperfect type translation exist. See the [TypeUtils](https://github.com/magicmq/docs-translator/blob/master/src/main/java/dev/magicmq/docstranslator/utils/TypeUtils.java) class for a better idea on how type translation is handled.

### JavaDoc Translation

Because JavaDoc strings are ultimately converted into HTML when generating JavaDocs for a Java project, usage of HTML tags/elements are allowed when writing JavaDoc strings. This presents a problem when translating JavaDoc strings to Python docstrings because Python docstrings, unlike JavaDoc strings, do not natively support HTML tags/elements. There are some docstring parsers that use markdown

Based on my own testing, it seems that the Pylance extension in VSCode, the IDE I am using to assess translation quality, interprets *some* (but not all) Markdown syntax. Therefore, I have attempted to translate as much as I can from HTML to Markdown, however, some HTML tags remain untranslated, namely:

- HTML tags pertaining to tables: `<table>`, `<th>`, `<tr>`, `<td>`, etc.
- HTML heading tags: `<h1>`, `<h2>`, `<h3>`, etc.
- Other miscellaneous tags: `<blockquote>`, and more
