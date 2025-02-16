const sidebars = {
  sidebar: [
    {
      type: "category",
      label: "ZIO Kafka",
      collapsed: false,
      link: { type: "doc", id: "index" },
      items: [
        "creating-a-consumer",
        "consuming-kafka-topics-using-zio-streams",
        "example-of-consuming-producing-and-committing-offsets",
        "partition-assignment-and-offset-retrieval",
        "metrics",
        "consumer-tuning",
        "preventing-duplicates",
        "sharing-consumer",
        "serialization-and-deserialization",
        "writing-tests",
        "migrating-to-2.11"
      ]
    }
  ]
};

module.exports = sidebars;
