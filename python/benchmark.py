import matplotlib.pyplot as plt
import numpy as np
import pandas as pd
import seaborn as sns

data = pd.read_csv(f"../benchmark.csv", sep=',')
data = data.groupby(['directory', 'type']).agg({'duration': 'median', 'actions': 'first', 'added': 'first',
                                                'deleted': 'first', 'unchanged': 'first', 'updated': 'first',
                                                'moved_unchanged': 'first', 'moved_updated': 'first'}).reset_index()
print(data.head())
data.to_csv("benchmark-merged.csv", index=False)

# Actions

sns.violinplot(data=data, x="type",
               hue="type", y="actions", palette="pastel", cut=0, inner="quartile")
plt.title("actions")
plt.xlabel("directory")
plt.ylabel("actions")
plt.xticks(rotation=45)
plt.tight_layout()
plt.savefig('benchmark-actions-violin.png')
plt.clf()

sns.pointplot(data=data, x="directory", hue="type", y="actions", palette="pastel")
plt.title("actions")
plt.xlabel("directory")
plt.ylabel("actions")
plt.xticks(rotation=45)
plt.tight_layout()
plt.savefig('benchmark-actions-pointplot.png')
plt.clf()

sns.swarmplot(data=data, x="type", hue="type", y="actions", palette="pastel")
plt.title("actions")
plt.xlabel("directory")
plt.ylabel("actions")
plt.xticks(rotation=45)
plt.tight_layout()
plt.savefig('benchmark-actions-swarmplot.png')
plt.clf()

sns.stripplot(data=data, x="type", hue="type", y="actions", palette="pastel")
plt.title("actions")
plt.xlabel("directory")
plt.ylabel("actions")
plt.xticks(rotation=45)
plt.tight_layout()
plt.savefig('benchmark-actions-stripplot.png')
plt.clf()

# Duration

sns.violinplot(data=data, x="type", hue="type", y="duration", palette="pastel", cut=0, inner="quartile")
plt.title("actions")
plt.xlabel("directory")
plt.ylabel("actions")
plt.xticks(rotation=45)
plt.tight_layout()
plt.savefig('benchmark-duration-violin.png')
plt.clf()

sns.pointplot(data=data, x="directory", hue="type", y="duration", palette="pastel")
plt.title("actions")
plt.xlabel("directory")
plt.ylabel("actions")
plt.xticks(rotation=45)
plt.tight_layout()
plt.savefig('benchmark-duration-pointplot.png')
plt.clf()

sns.swarmplot(data=data, x="type", hue="type", y="duration", palette="pastel")
plt.title("actions")
plt.xlabel("directory")
plt.ylabel("actions")
plt.xticks(rotation=45)
plt.tight_layout()
plt.savefig('benchmark-duration-swarmplot.png')
plt.clf()

sns.stripplot(data=data, x="type", hue="type", y="duration", palette="pastel")
plt.title("actions")
plt.xlabel("directory")
plt.ylabel("actions")
plt.xticks(rotation=45)
plt.tight_layout()
plt.savefig('benchmark-duration-stripplot.png')

