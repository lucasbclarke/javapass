### javapass

javapass is a simple password generation and management tool. javapass has two commands "generate" and "find".
- Generate: Creates a new password based on a random selection of all lower and upcase case letters, all numbers and all symbols able to be printed from a standard keyboard.
            This password is always 20 characters long and after generation, is copied into the users clipboard using wl-copy and a notification is provided through notify-send.

- Find: Finds a password from a given service within a given secrets.yaml file. This is designed to be used inconjunction with the [sops](https://github.com/getsops/sops) secrets
        management tool. The user provides the service they wish to retrieve the password for as their first argument, and then the location of the secretes.yaml file on their system
        as the second argument.
