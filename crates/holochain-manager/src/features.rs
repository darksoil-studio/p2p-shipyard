#[cfg(all(feature = "gossip_arc_empty", feature = "gossip_arc_full"))]
compile_error!(
    "The `gossip_arc_empty` and `gossip_arc_full` features are both enabled, which is an error. Please enable only one."
);
#[cfg(all(feature = "gossip_arc_empty", feature = "gossip_arc_normal"))]
compile_error!(
    "The `gossip_arc_empty` and `gossip_arc_normal` features are both enabled, which is an error. Please enable only one."
);
#[cfg(all(feature = "gossip_arc_full", feature = "gossip_arc_normal"))]
compile_error!(
    "The `gossip_arc_full` and `gossip_arc_normal` features are both enabled, which is an error. Please enable only one."
);

#[cfg(all(not(feature = "gossip_arc_empty"), not(feature = "gossip_arc_full"), not(feature = "gossip_arc_normal")))]
compile_error!("All of the `gossip_arc_empty`, `gossip_arc_full`, and `gossip_arc_normal` features are disabled. Please enable one of them.");
