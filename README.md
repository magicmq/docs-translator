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
2. Java source JAR files (I.E. those that follow the format `*-sources.jar`) from remote repositories/URLs defined in the `settings.yml`.
3. The application loops through all files contained within downloaded source JAR files. When it encounters a Java soruce file (ending in `.java`), the file is parsed with JavaParser and a best effort attempt is made to translate the source file into Python code.
    * Any files not ending in `.java` are ignored.
4. Translated `.py` files are placed in the user-defined output folder (`generated` by default), in the appropriate package.
5. An entry is added to the `__init__.py` file in the appropriate package, to allow for importing the python module as one would normally import a Java class in Jython.
6. Any source files from the Java Standard Library utilized by the previously translated Java source files are also translated in the same process outlined above.
    * JDK sources must be downloaded manually and placed in the appropriate folder (`jdk-sources` by default)
    * This step is only completed if enabled in the `settings.yml` (via the `jdkSources.translate` option)
7. All `__init__.py` files are generated and placed in their appropriate locations.
8. `setup.py` and `pyproject.toml` files are generated from options specified in the `settings.yml` and are placed in the user-defined output folder (`generated` by default).

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

The `settings.yml` file is the main configuration file for the project. Options are outlined below.

### `general`:

General options.

- `loggingLevel`: The mimum logging level for a message to be logged to console and to the `output.log` file.

### `sourceJars`:

Options pertaining to source JAR files to be translated.

- `path`: The path to the folder where source JAR files are downloaded and placed.
- `deleteOnStart`: If set to `true`, the JARs folder will be deleted when DocsTranslator first runs.
- `download`: If set to `true`, JAR files listed in the `urls` section will be downloaded
- `urls`: A list of URLs pointing to source JAR files that should be downloaded.

### `jdkSources`:

Options pertaining to sources from the Java Standard Library.

- `translate`: If set to `true`, any utilized Java Standard Library source files will also be translated.
- `path`: The path to the folder where the Java Standard Library sources are located.
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
- `params`: The format for the docstring parameters section.
- `param`: The format for the JavaDoc `@param` tag.
- `typeParam`: The format for any defined type parameter.
- `returns`: The format for the JavaDoc `@returns` tag.
- `see`: The format for the JavaDoc `@see` tag.
- `serial`: The format for the JavaDoc `@serial` tag.
- `serialData`: The format for the JavaDoc `@serialdata` tag.
- `serialField`: The format for the JavaDoc `@serialfield` tag.
- `since`: The format for the JavaDoc `@since` tag.
- `throws`: The format for the docstring throws section.
- `throw`: The format for the JavaDoc `@throws` tag.
- `version`: The format for the JavaDoc `@version` tag.
- `unknown`: The format for any unknown or unparseable JavaDoc tag.

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

## Caveats/Known Issues

### Type Translation

Because Java is statically-typed, but Python is not, translation of Java types to Python types is not perfect. For example:

* The Java `Collection` type is translated to a Python `Iterable`, although these types are not wholly interchangeable.
* Generics are not fully translated. This could be attempted with the `TypeVar` class (available in Python's `typing` module), however, I did not pursue this given that it would become quite completed with Java classes that contain several generic methods.
* Other examples of imperfect type translation exist. See the [TypeUtils](https://github.com/magicmq/docs-translator/blob/master/src/main/java/dev/magicmq/docstranslator/utils/TypeUtils.java) class for a better idea on how type translation is handled.
