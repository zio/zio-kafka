const sidebars = {
  sidebar: [
    "index",
    "getting-started",
    {
      type: "category",
      label: "Reference",
      collapsed: false,
      items: [
        {
          type: "category",
          label: "Consumer",
          collapsed: false,
          items: [
            "consumer",
            "consumer-settings",
            "partition-assignment-and-offset-retrieval",
            "offsets-and-committing"
          ]
        },
        {
          type: "category",
          label: "Producer",
          collapsed: false,
          items: ["producer", "transactional-producer"]
        },
        {
          type: "category",
          label: "Serialization",
          collapsed: false,
          items: ["serialization-and-deserialization", "record-headers"]
        },
        "admin-client",
        "security",
        {
          type: "category",
          label: "Observability",
          collapsed: false,
          items: ["diagnostics", "metrics", "metrics-observers"]
        },
        "testkit"
      ]
    },
    {
      type: "category",
      label: "Tutorials",
      collapsed: true,
      items: ["tutorial", "transactions"]
    },
    {
      type: "category",
      label: "How-to Guides",
      collapsed: true,
      items: [
        "creating-a-consumer",
        "consuming-kafka-topics-using-zio-streams",
        "sharing-consumer",
        "preventing-duplicates",
        "consumer-tuning",
        "consuming-producing-and-committing-offsets",
        "avoiding-chunk-breakers",
        "connecting-to-a-secured-cluster",
        "detecting-authorization-failures",
        "writing-tests"
      ]
    },
    {
      type: "category",
      label: "Migration Guides",
      collapsed: true,
      items: ["migrating-to-zio-kafka-4", "migrating-to-zio-kafka-3"]
    }
  ]
};

module.exports = sidebars;
