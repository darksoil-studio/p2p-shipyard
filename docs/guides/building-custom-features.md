# Building custom features

Let's now finish building our app, by implementing those features that are unique to it and are not covered in any of the modules.

### Scaffold your zomes

In holochain, each DNA is composed out of multiple [zomes](https://developer.holochain.org/build/zomes/). Each zome defines:

- Entry types: the data types that exist in your app.
- Collections of entries, eg. to get all entries of a particular type that exist in the network.
- Link types: links go from a particular entry to another.
- API calls: the functions that the frontend is able to call to read or write data from the network.

First, for each of your DNAs, think which zomes it needs on top of the ones that you imported with the p2p Shipyard modules.

To create zomes, go into the app's folder and run:

```bash
hc scaffold zome
```

and follow its instructions. This will generate both backend and frontend code.

Then, design which entry and link types will each zome have.

Create entry types with:

```bash
hc scaffold entry-type
```

Create collections with:

```bash
hc scaffold collection
```

Usually you'll want one of the generated collection components imported and rendered as the main entry point component of your app.

Lastly, create any link types that you'd need with:

```bash
hc scaffold link-type
```

Good job! You now have a great starting point of generated code for your zome.

Now it's up to you to take it on and finish its building.

You can run this command to see how your app looks like.

::: code-group
```bash [npm]
npm start
```

```bash [yarn]
yarn start
```

```bash [pnpm]
pnpm start
```
:::


Have fun coding!

---

When your app is ready, come back to [distribution](/guides/distribution) to learn how to ship it to your users.
