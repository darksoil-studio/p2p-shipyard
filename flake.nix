{
  description = "Build cross-platform holochain apps and runtimes";

  inputs = {
    holochain-utils.url = "github:darksoil-studio/holochain-utils/main-0.5";
    nixpkgs.follows = "holochain-utils/nixpkgs";
    flake-parts.follows = "holochain-utils/holonix/flake-parts";

    linked-devices-zome.url =
      "github:darksoil-studio/linked-devices-zome/main-0.5";
    linked-devices-zome.inputs.holochain-utils.follows = "holochain-utils";

    file-storage.url = "github:darksoil-studio/file-storage/main-0.5";
    file-storage.inputs.holochain-utils.follows = "holochain-utils";

    profiles-zome.url = "github:darksoil-studio/profiles-zome/main-0.5";
    profiles-zome.inputs.holochain-utils.follows = "holochain-utils";
    profiles-zome.inputs.linked-devices-zome.follows = "linked-devices-zome";

    notifications-zome.url = "github:darksoil-studio/friends-zome/main-0.5";
    notifications-zome.inputs.holochain-utils.follows = "holochain-utils";
    notifications-zome.inputs.linked-devices-zome.follows =
      "linked-devices-zome";

    roles-zome.url = "github:darksoil-studio/friends-zome/main-0.5";
    roles-zome.inputs.holochain-utils.follows = "holochain-utils";
    roles-zome.inputs.linked-devices-zome.follows = "linked-devices-zome";

    friends-zome.url = "github:darksoil-studio/friends-zome/main-0.5";
    friends-zome.inputs.holochain-utils.follows = "holochain-utils";
    friends-zome.inputs.linked-devices-zome.follows = "linked-devices-zome";

    messenger-zome.url = "github:darksoil-studio/messenger-zome/main-0.5";
    messenger-zome.inputs.holochain-utils.follows = "holochain-utils";
    messenger-zome.inputs.profiles-zome.follows = "profiles-zome";
    messenger-zome.inputs.linked-devices-zome.follows = "linked-devices-zome";

    push-notifications-service.url =
      "github:darksoil-studio/push-notifications-service/main-0.5";
    push-notifications-service.inputs.holochain-utils.follows =
      "holochain-utils";

    safehold.url = "github:darksoil-studio/safehold/main-0.5";
    safehold.inputs.holochain-utils.follows = "holochain-utils";

    always-online-nodes.url = "github:darksoil-studio/always-online-nodes/main";
    always-online-nodes.inputs.holochain-utils.follows = "holochain-utils";
  };

  nixConfig = {
    extra-substituters = [
      "https://holochain-ci.cachix.org"
      "https://darksoil-studio.cachix.org"
    ];
    extra-trusted-public-keys = [
      "holochain-ci.cachix.org-1:5IUSkZc0aoRS53rfkvH9Kid40NpyjwCMCzwRTXy+QN8="
      "darksoil-studio.cachix.org-1:UEi+aujy44s41XL/pscLw37KEVpTEIn8N/kn7jO8rkc="
    ];
  };

  outputs = inputs@{ ... }:
    inputs.holochain-utils.inputs.holonix.inputs.flake-parts.lib.mkFlake {
      inherit inputs;
    } {

      flake.flakeModules = {
        builders = inputs.holochain-utils.outputs.flakeModules.builders;
        dependencies = inputs.holochain-utils.outputs.flakeModules.dependencies;
      };
      imports = [
        inputs.holochain-utils.outputs.flakeModules.builders
        inputs.holochain-utils.outputs.flakeModules.dependencies
      ];

      systems =
        builtins.attrNames inputs.holochain-utils.inputs.holonix.devShells;
      perSystem = { inputs', config, self', pkgs, system, lib, ... }: {

        builders.rustZome = inputs'.holochain-utils.builders.rustZome;
        builders.dna = inputs'.holochain-utils.builders.dna;
        builders.happ = inputs'.holochain-utils.builders.happ;
        builders.webhapp = inputs'.holochain-utils.builders.webhapp;

        dependencies.holochain = inputs'.holochain-utils.dependencies.holochain;

        devShells =
          inputs.holochain-utils.inputs.tauri-plugin-holochain.outputs.devShells.${system}
          // {
            synchronized-pnpm =
              inputs'.holochain-utils.devShells.synchronized-pnpm;

            default = pkgs.mkShell {
              inputsFrom = [ inputs'.holochain-utils.devShells.default ];
              packages = [ pkgs.pnpm ];
            };

            holochainDev = inputs'.holochain-utils.devShells.holochainDev;
          };

        packages = inputs'.holochain-utils.packages
          // inputs'.linked-devices-zome.packages
          // inputs'.file-storage.packages // inputs'.profiles-zome.packages
          // inputs'.roles-zome.packages // inputs'.friends-zome.packages
          // inputs'.messenger-zome.packages
          // inputs'.always-online-nodes.packages
          // inputs'.push-notifications-service.packages
          // inputs'.safehold.packages // { };
      };
    };
}
