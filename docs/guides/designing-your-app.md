# Designing your app

Holochain apps can include multiple [DNAs](https://developer.holochain.org/build/dnas/). DNAs define the rules of the network: which actions are allowed, and which ones are declared invalid. When instantiated, each DNA participates in a different network.

Holochain networks can be:

a) **Accessible to everyone**, in which case all data published to the network is public.

or

b) **Accessible only to users that fulfill an authentication check**, in which case data published to the network is accessible only to those users.

If you want to learn more about how holochain works, check out this video:

<iframe width="688" height="400" src="https://www.youtube.com/embed/-Q6uursAggY?si=Mknt4RC7Fw9HwIXC" title="YouTube video player" frameborder="0" allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share" referrerpolicy="strict-origin-when-cross-origin" allowfullscreen></iframe>

### Design your DNAs

With this in mind, **design which DNAs your app will have**.

Here are some questions that will help with that:

- What are the types of data that exist within your app?
- What user roles exist within your app?
- Which types of data should each user role be able to access?

### Example 
<details>
<summary>Click here to see how an example for an app design could look like.</summary>

Let's imagine we are building a social media app, with two main features:

- Users can create public posts, that all users can see.
- Users can create private invite-only communities, and create posts there only available to the invited members of the community.

Here is how the design for this app could look like:

User roles:
- Author of public posts.
- Community admin.
- Community member.

Types of data:
- Public posts: accessible to all users.
- Community profile: accessible only to community admins and members.
- Community posts: accessible only to community admins and members. 

Our app is going to have 2 DNAs:
- `open_space`: open to everyone, all posts are public.
- `community`:
  - Each instantiation of this DNA will create its own network.
  - Each network will have an admin.
  - Only users with an invitation by the admin can access.
  - Posts can only be seen by the members of a community.

</details>

### Creating DNAs

Great! Now that you've designed your app and now which DNAs will exist, it's time to create those DNAs.

Go into your app's folder, and run this command for each DNA in your design:

```bash
hc scaffold dna
```

And enter the name of your DNA to complete the command.

Awesome work! Now it's time to [import modules from the p2p Shipyard](/guides/importing-modules).
