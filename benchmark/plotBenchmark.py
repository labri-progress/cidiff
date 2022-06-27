#!/usr/bin/env python3

import seaborn as sns
import matplotlib.pyplot as plt
import pandas as pd 

sns.set_theme(style="whitegrid", palette="colorblind")

algorithms = ["ABF", "BF", "LCS", "SE", "H"]

#load dataframe
path_to_csv = "benchmark/precision_recall.csv"
df = pd.read_csv(path_to_csv, sep = ';', index_col=False)
df = df.melt(id_vars =['LEFT','RIGHT','ALGORITHM'], 
            value_vars =["PRECISION","RECALL","FSCORE","TIME"],
            var_name ='metric')

#plot precision/recall/F-score
df_pr = df[df['metric'].isin(['PRECISION','RECALL'])]
ax = sns.violinplot(data=df_pr, x='ALGORITHM', y='value', hue="metric", palette="pastel", cut=0)
sns.stripplot(data=df_pr, x='ALGORITHM', y='value', hue="metric", dodge=True)
ax.set_xticklabels(algorithms)
handles, labels = ax.get_legend_handles_labels()
ax.legend(handles=handles[0:2], labels=["Precision", "Recall"])
plt.xlabel("Algorithm")
plt.ylabel("Precision / Recall")
plt.title("Precision and recall scores")
plt.savefig('benchmark/fig-p-r.png')
plt.clf()

#plot F-score
df_fscore = df[df['metric'].isin(['FSCORE'])]
ax = sns.violinplot(data=df_fscore, x='ALGORITHM', y='value', palette="pastel", cut=0)
sns.stripplot(data=df_fscore, x='ALGORITHM', y='value')
ax.set_xticklabels(algorithms)
plt.xlabel("Algorithm")
plt.ylabel("F-score")
plt.title("F-scores")
plt.savefig('benchmark/fig-f1.png')
plt.clf()

#plot time
df_time = df[df['metric'].isin(['TIME'])]
ax = sns.violinplot(data=df_time, x='ALGORITHM', y='value', palette="pastel", cut=0)
sns.stripplot(data=df_time, x='ALGORITHM', y='value')
ax.set_xticklabels(algorithms)
plt.xlabel("Algorithm")
plt.ylabel("Execution time (ms)")
plt.title("Execution times")
plt.savefig('benchmark/fig-t.png')