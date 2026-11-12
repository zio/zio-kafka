const sidebars = {
  sidebar: [
    {
      type: "category",
      label: "ZIO Kafka",
      collapsed: false,
      link: { type: "doc", id: "index" },
      items: [
        "getting-started",
        {
          type: "category",
          label: "Reference",
          collapsed: false,
          items: [
            "consumer",
            "partition-assignment-and-offset-retrieval",
            "consumer-tuning",
            "offsets-and-committing",
            "producer",
            "transactions",
            "serialization-and-deserialization",
            "record-headers",
            "admin-client",
            "diagnostics",
            "metrics",
            "security"
          ]
        },
        {
          type: "category",
          label: "Tutorials",
          collapsed: false,
          items: [
            "tutorial"
          ]
        },
        {
          type: "category",
          label: "How-to Guides",
          collapsed: false,
          items: [
            "creating-a-consumer",
            "consuming-kafka-topics-using-zio-streams",
            "consuming-producing-and-committing-offsets",
            "sharing-consumer",
            "preventing-duplicates",
            "avoiding-chunk-breakers",
            "detecting-authorization-failures",
            "writing-tests",
            "migrating-to-zio-kafka-4",
            "migrating-to-zio-kafka-3"
          ]
        }
      ]
    }
  ]
};

module.exports = sidebars;
