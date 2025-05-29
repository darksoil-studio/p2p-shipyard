import { defineConfig } from "vitepress";

// https://vitepress.dev/reference/site-config
export default defineConfig({
  title: "p2p Shipyard",
  description: "peer-to-peer apps made easy",
  base: "/p2p-shipyard",
  themeConfig: {
    // https://vitepress.dev/reference/default-theme-config
    nav: [
      {
        text: "Guides",
        link: "/guides/overview",
      },
      // {
      //   text: "Blog",
      //   link: "/blog/how-to-create-an-end-user-happ",
      // },
    ],

    sidebar: {
      "/guides/": [
        {
          text: "Overview",
          link: "/guides/overview",
        },
        {
          text: "Build",
          items: [
            {
              text: "Creating an app",
              link: "/guides/creating-an-app",
            },
            {
              text: "Designing the app",
              link: "/guides/designing-your-app",
            },
            {
              text: "Importing modules",
              // collapsed: true,
              link: "/guides/importing-modules",
              items: [
                {
                  text: "Profiles",
                  link: "https://darksoil.studio/profiles-zome",
                },
                {
                  text: "Linked Devices",
                  link: "https://darksoil.studio/-zome",
                },
                {
                  text: "File Storage",
                  link: "https://darksoil.studio/file-storage",
                },
                {
                  text: "Friends",
                  link: "https://darksoil.studio/friends-zome",
                },
                {
                  text: "Messenger",
                  link: "https://darksoil.studio/messenger-zome",
                },
                {
                  text: "Roles",
                  link: "https://darksoil.studio/roles-zome",
                },
                {
                  text: "Notifications",
                  link: "https://darksoil.studio/notifications-zome",
                },
                {
                  text: "Collaborative Sessions",
                  link: "https://darksoil.studio/collaborative-sessions",
                },
                {
                  text: "Notes",
                  link: "https://darksoil.studio/notes-zome",
                },
                {
                  text: "Comments",
                  link: "https://darksoil.studio/comments-zome",
                },
                {
                  text: "Tags",
                  link: "https://darksoil.studio/tags-zome",
                },
                {
                  text: "Chain of Custody",
                  link: "https://darksoil.studio/chain-of-custody-zome",
                },
                {
                  text: "Private Events Engine",
                  link: "https://darksoil.studio/private-event-sourcing-zome",
                },
                {
                  text: "Membrane Invitations",
                  link: "https://darksoil.studio/membrane-invitations-zome",
                },
              ],
            },
            {
              text: "Building custom features",
              link: "/guides/building-custom-features",
            },
            {
              text: "Distribution",
              link: "/guides/distribution",
              collapsed: true,
              items: [
                {
                  text: "Android",
                  items: [
                    {
                      text: "Project Setup",
                      link: "/guides/android/project-setup",
                    },
                    {
                      text: "Device Setup",
                      link: "/guides/android/device-setup",
                    },
                    {
                      text: "Developing",
                      link: "/guides/android/developing",
                    },
                    {
                      text: "Shipping",
                      link: "/guides/android/shipping",
                    },
                  ],
                },
              ],
            },
          ],
        },
      ],
    },

    socialLinks: [
      {
        icon: "github",
        link: "https://github.com/darksoil-studio/p2p-shipyard",
      },
    ],
  },
});
