const sidebars = {
  sidebar: [
    {
      type: "category",
      label: "ZIO Kafka",
      collapsed: false,
      link: { type: "doc", id: "index" },
      items: [
        {
          type: "category",
          label: "Reference",
          collapsed: true,
          items: [
            "partition-assignment-and-offset-retrieval",
            "serialization-and-deserialization",
            "transactions",
            "metrics"
          ]
        },
        {
          type: "category",
          label: "Tutorial",
          collapsed: true,
          items: [
            "tutorial"
          ]
        },
        {
          type: "category",
          label: "How-to Guides",
          collapsed: true,
          items: [
            "creating-a-consumer",
            "consuming-kafka-topics-using-zio-streams",
            "consuming-producing-and-committing-offsets",
            "sharing-consumer",
            "preventing-duplicates",
            "avoiding-chunk-breakers",
            "consumer-tuning",
            "detecting-authorization-failures",
            "writing-tests",
            "migrating-to-zio-kafka-3"
          ]
        }
      ]
    }
  ]
};

module.exports = sidebars;
